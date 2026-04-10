const {setGlobalOptions} = require("firebase-functions");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const {getFirestore, FieldValue} = require("firebase-admin/firestore");
const {initializeApp} = require("firebase-admin/app");
const {Configuration, PlaidApi, PlaidEnvironments} = require("plaid");

// Initialize Firebase Admin
initializeApp();
const db = getFirestore("financetracker");

setGlobalOptions({maxInstances: 10});

// ─── Plaid Client Setup ─────────────────────────────────────────────
// Lazy initialization — secrets are only available at function call time,
// not at module load time, so we create the client on first use.
let _plaidClient = null;
function getPlaidClient() {
  if (!_plaidClient) {
    const config = new Configuration({
      basePath: PlaidEnvironments[process.env.PLAID_ENV || "sandbox"],
      baseOptions: {
        headers: {
          "PLAID-CLIENT-ID": process.env.PLAID_CLIENT_ID,
          "PLAID-SECRET": process.env.PLAID_SECRET,
        },
      },
    });
    _plaidClient = new PlaidApi(config);
  }
  return _plaidClient;
}

/**
 * Extracts a Plaid error code from an error object, falling back to a
 * generic message if none is found.
 */
function getPlaidErrorMessage(error, fallback) {
  const plaidCode = error.response?.data?.error_code;
  if (plaidCode) return plaidCode;
  return fallback;
}

// ─── Rate Limiting ──────────────────────────────────────────────────
// Limits each user to 10 Plaid API calls per hour.
// Tracks timestamps in rate_limits/{userId} using a Firestore transaction.
const RATE_LIMIT_MAX_CALLS = 30;
const RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000; // 1 hour

async function checkRateLimit(userId) {
  const ref = db.collection("rate_limits").doc(userId);
  const now = Date.now();
  const windowStart = now - RATE_LIMIT_WINDOW_MS;

  return db.runTransaction(async (transaction) => {
    const doc = await transaction.get(ref);
    const data = doc.exists ? doc.data() : {calls: []};
    const recentCalls = data.calls.filter((ts) => ts > windowStart);

    if (recentCalls.length >= RATE_LIMIT_MAX_CALLS) {
      throw new HttpsError(
          "resource-exhausted",
          "Rate limit exceeded. Please wait before trying again.",
      );
    }

    recentCalls.push(now);
    transaction.set(ref, {calls: recentCalls});
  });
}

// ─── 1. Create Link Token ───────────────────────────────────────────
// Called from Android to get a link_token for opening Plaid Link
exports.createLinkToken = onCall(
    {
      secrets: ["PLAID_CLIENT_ID", "PLAID_SECRET", "PLAID_ENV"],
    },
    async (request) => {
      // Ensure user is authenticated
      if (!request.auth) {
        throw new HttpsError(
            "unauthenticated",
            "User must be signed in to link a bank account.",
        );
      }

      const userId = request.auth.uid;
      await checkRateLimit(userId);

      try {
        const response = await getPlaidClient().linkTokenCreate({
          user: {client_user_id: userId},
          client_name: "Finance Tracker",
          products: ["transactions"],
          country_codes: ["US"],
          language: "en",
          android_package_name: "com.jonathan.financetracker",
        });

        logger.info("Link token created for user", {userId});
        return {linkToken: response.data.link_token};
      } catch (error) {
        logger.error("PLAID ERROR DETAIL", {
          status: error.response?.status,
          data: JSON.stringify(error.response?.data),
          message: error.message,
        });
        throw new HttpsError(
            "internal",
            getPlaidErrorMessage(error, "Failed to create link token."),
        );
      }
    },
);

// ─── 2. Exchange Public Token ────────────────────────────────────────
// Called after user completes Plaid Link — exchanges public_token
// for a permanent access_token and stores it in Firestore
exports.exchangePublicToken = onCall(
    {
      secrets: ["PLAID_CLIENT_ID", "PLAID_SECRET", "PLAID_ENV"],
    },
    async (request) => {
      if (!request.auth) {
        throw new HttpsError(
            "unauthenticated",
            "User must be signed in.",
        );
      }

      const userId = request.auth.uid;
      await checkRateLimit(userId);
      const {publicToken, institutionName, institutionId} = request.data;

      if (!publicToken) {
        throw new HttpsError(
            "invalid-argument",
            "publicToken is required.",
        );
      }

      try {
        // Exchange public token for access token
        const response = await getPlaidClient().itemPublicTokenExchange({
          public_token: publicToken,
        });

        const accessToken = response.data.access_token;
        const itemId = response.data.item_id;

        // Store in Firestore under the user's plaid_items subcollection
        await db.collection("plaid_items").doc(itemId).set({
          ownerId: userId,
          accessToken: accessToken,
          itemId: itemId,
          institutionName: institutionName || "",
          institutionId: institutionId || "",
          cursor: null, // For incremental transaction sync
          createdAt: FieldValue.serverTimestamp(),
        });

        logger.info("Public token exchanged", {userId, itemId});
        return {success: true, itemId};
      } catch (error) {
        logger.error("Error exchanging public token", {
          error: error.message,
          plaidCode: error.response?.data?.error_code,
        });
        throw new HttpsError(
            "internal",
            getPlaidErrorMessage(error, "Failed to link bank account."),
        );
      }
    },
);

// ─── 3. Sync Transactions ────────────────────────────────────────────
// Fetches new transactions from Plaid using the sync API and writes
// them to the user's transactions collection in Firestore
exports.syncTransactions = onCall(
    {
      secrets: ["PLAID_CLIENT_ID", "PLAID_SECRET", "PLAID_ENV"],
    },
    async (request) => {
      if (!request.auth) {
        throw new HttpsError(
            "unauthenticated",
            "User must be signed in.",
        );
      }

      const userId = request.auth.uid;
      await checkRateLimit(userId);

      try {
        // Get all Plaid items for this user
        const itemsSnapshot = await db
            .collection("plaid_items")
            .where("ownerId", "==", userId)
            .get();

        if (itemsSnapshot.empty) {
          return {added: 0, message: "No linked accounts found."};
        }

        let totalAdded = 0;

        // 60-day cutoff for transaction history
        const cutoffDate = new Date();
        cutoffDate.setDate(cutoffDate.getDate() - 60);
        const cutoffStr = cutoffDate.toISOString().substring(0, 10);

        for (const itemDoc of itemsSnapshot.docs) {
          const item = itemDoc.data();
          let cursor = item.cursor;
          let hasMore = true;

          while (hasMore) {
            const syncResponse = await getPlaidClient().transactionsSync({
              access_token: item.accessToken,
              cursor: cursor || undefined,
            });

            const {added, modified, removed, next_cursor, has_more} =
                syncResponse.data;

            // Process added transactions
            const batch = db.batch();
            for (const txn of added) {
              // Skip transactions older than 60 days
              if (txn.date < cutoffStr) continue;

              const yearMonth = txn.date.substring(0, 7); // "yyyy-MM"

              const docRef = db.collection("transactions").doc();
              batch.set(docRef, {
                description: txn.name || txn.merchant_name || "Unknown",
                amount: Math.abs(txn.amount),
                date: new Date(txn.date),
                type: txn.amount < 0 ? "Income" : "Expense",
                methodOfPayment: txn.payment_channel || "Other",
                budgetName: "",
                budgetId: "",
                ownerId: userId,
                yearMonth: yearMonth,
                isManuallyCreated: false,
                plaidTransactionId: txn.transaction_id,
                plaidCategory: txn.category
                    ? txn.category.join(", ")
                    : "",
                plaidItemId: item.itemId,
              });
            }

            // Process removed transactions
            if (removed.length > 0) {
              const removedIds = removed.map((r) => r.transaction_id);
              const existingTxns = await db
                  .collection("transactions")
                  .where("ownerId", "==", userId)
                  .where("plaidTransactionId", "in", removedIds.slice(0, 10))
                  .get();
              existingTxns.forEach((doc) => batch.delete(doc.ref));
            }

            await batch.commit();
            totalAdded += added.length;

            // Update cursor for incremental sync
            cursor = next_cursor;
            hasMore = has_more;
          }

          // Save the cursor so next sync is incremental
          await itemDoc.ref.update({cursor});
        }

        logger.info("Transaction sync complete", {userId, totalAdded});
        return {added: totalAdded, message: `Synced ${totalAdded} transactions.`};
      } catch (error) {
        logger.error("Error syncing transactions", {
          error: error.message,
          plaidCode: error.response?.data?.error_code,
        });
        throw new HttpsError(
            "internal",
            getPlaidErrorMessage(error, "Failed to sync transactions."),
        );
      }
    },
);

// ─── 4. Get Linked Accounts ─────────────────────────────────────────
// Returns the list of linked bank accounts for the user (without access tokens)
exports.getLinkedAccounts = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "User must be signed in.");
  }

  const userId = request.auth.uid;

  try {
    const itemsSnapshot = await db
        .collection("plaid_items")
        .where("ownerId", "==", userId)
        .get();

    const accounts = itemsSnapshot.docs.map((doc) => ({
      itemId: doc.data().itemId,
      institutionName: doc.data().institutionName,
      createdAt: doc.data().createdAt,
    }));

    return {accounts};
  } catch (error) {
    logger.error("Error fetching linked accounts", {error: error.message});
    throw new HttpsError("internal", "Failed to fetch linked accounts.");
  }
});

// ─── 5. Unlink Account ──────────────────────────────────────────────
// Removes a linked bank account and its synced transactions
exports.unlinkAccount = onCall(
    {
      secrets: ["PLAID_CLIENT_ID", "PLAID_SECRET", "PLAID_ENV"],
    },
    async (request) => {
      if (!request.auth) {
        throw new HttpsError("unauthenticated", "User must be signed in.");
      }

      const userId = request.auth.uid;
      await checkRateLimit(userId);
      const {itemId} = request.data;

      if (!itemId) {
        throw new HttpsError("invalid-argument", "itemId is required.");
      }

      try {
        // Get the item to verify ownership
        const itemDoc = await db.collection("plaid_items").doc(itemId).get();

        if (!itemDoc.exists || itemDoc.data().ownerId !== userId) {
          throw new HttpsError("not-found", "Linked account not found.");
        }

        // Remove from Plaid (ignore errors for tokens from a different environment)
        try {
          await getPlaidClient().itemRemove({
            access_token: itemDoc.data().accessToken,
          });
        } catch (plaidError) {
          logger.warn("Plaid itemRemove failed (stale token?), cleaning up locally", {
            itemId,
            plaidCode: plaidError.response?.data?.error_code,
          });
        }

        // Delete synced transactions from this item
        const txnSnapshot = await db
            .collection("transactions")
            .where("ownerId", "==", userId)
            .where("plaidItemId", "==", itemId)
            .get();

        const batch = db.batch();
        txnSnapshot.forEach((doc) => batch.delete(doc.ref));
        batch.delete(itemDoc.ref);
        await batch.commit();

        logger.info("Account unlinked", {userId, itemId});
        return {success: true};
      } catch (error) {
        logger.error("Error unlinking account", {
          error: error.message,
          plaidCode: error.response?.data?.error_code,
        });
        throw new HttpsError(
            "internal",
            getPlaidErrorMessage(error, "Failed to unlink account."),
        );
      }
    },
);
