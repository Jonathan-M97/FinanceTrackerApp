package com.jonathan.financetracker.data.model

data class LinkedAccount(
    val itemId: String = "",
    val institutionName: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val status: LinkStatus = LinkStatus.OK
)

enum class LinkStatus {
    /** Item is healthy — last sync succeeded (or hasn't run yet). */
    OK,

    /** Item needs the user to re-authenticate via Plaid update mode. */
    NEEDS_REAUTH,

    /** Last sync failed for some other reason (network, Plaid outage, etc.). */
    ERROR;

    companion object {
        /**
         * Parses the status string written by the Cloud Function. Unknown
         * values fall back to OK so a future server-side status name
         * doesn't crash older clients.
         */
        fun fromString(raw: String?): LinkStatus = when (raw) {
            "needs_reauth" -> NEEDS_REAUTH
            "error" -> ERROR
            else -> OK
        }
    }
}
