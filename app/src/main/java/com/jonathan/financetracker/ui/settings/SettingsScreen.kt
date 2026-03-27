package com.jonathan.financetracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.LinkedAccount
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.StandardButton
import com.jonathan.financetracker.ui.theme.FinanceTrackerTheme
import com.plaid.link.OpenPlaidLink
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

@Composable
fun SettingsScreen(
    openDashboard: () -> Unit,
    openSignInScreen: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val shouldRestartApp by viewModel.shouldRestartApp.collectAsStateWithLifecycle()

    if (shouldRestartApp) {
        openDashboard()
    } else {
        val isAnonymous by viewModel.isAnonymous.collectAsStateWithLifecycle()
        val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
        val linkedAccounts by viewModel.linkedAccounts.collectAsStateWithLifecycle()
        val linkToken by viewModel.linkToken.collectAsStateWithLifecycle()
        val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
        val syncResultMessage by viewModel.syncResultMessage.collectAsStateWithLifecycle()

        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        // Show sync result as snackbar
        LaunchedEffect(syncResultMessage) {
            syncResultMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.onSyncMessageShown()
            }
        }

        // Show errors as snackbar
        LaunchedEffect(errorMessage) {
            errorMessage?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.onErrorShown()
            }
        }

        // Launch Plaid Link when a link token is available
        val plaidLauncher = rememberLauncherForActivityResult(
            contract = OpenPlaidLink()
        ) { result ->
            when (result) {
                is LinkSuccess -> {
                    val publicToken = result.publicToken
                    val institution = result.metadata.institution
                    viewModel.exchangePublicToken(
                        publicToken = publicToken,
                        institutionName = institution?.name ?: "",
                        institutionId = institution?.id ?: ""
                    )
                }
                is LinkExit -> {
                    result.error?.let { error ->
                        viewModel.onPlaidLinkError(
                            error.displayMessage ?: error.errorMessage
                        )
                    }
                }
            }
        }

        LaunchedEffect(linkToken) {
            linkToken?.let { token ->
                val config = LinkTokenConfiguration.Builder()
                    .token(token)
                    .build()
                plaidLauncher.launch(config)
                viewModel.onLinkTokenConsumed()
            }
        }

        SettingsScreenContent(
            loadCurrentUser = viewModel::loadCurrentUser,
            loadLinkedAccounts = viewModel::loadLinkedAccounts,
            openSignInScreen = openSignInScreen,
            signOut = viewModel::signOut,
            deleteAccount = viewModel::deleteAccount,
            isAnonymous = isAnonymous,
            userEmail = userEmail,
            linkedAccounts = linkedAccounts,
            onLinkBankAccount = viewModel::createLinkToken,
            onSyncTransactions = viewModel::syncTransactions,
            onUnlinkAccount = viewModel::unlinkAccount,
            isSyncing = isSyncing,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreenContent(
    loadCurrentUser: () -> Unit,
    loadLinkedAccounts: () -> Unit,
    openSignInScreen: () -> Unit,
    signOut: () -> Unit,
    deleteAccount: () -> Unit,
    isAnonymous: Boolean,
    userEmail: String?,
    linkedAccounts: List<LinkedAccount>,
    onLinkBankAccount: () -> Unit,
    onSyncTransactions: () -> Unit,
    onUnlinkAccount: (String) -> Unit,
    isSyncing: Boolean,
    snackbarHostState: SnackbarHostState
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(true) {
        loadCurrentUser()
        loadLinkedAccounts()
    }

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp,
                    bottom = innerPadding.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.size(24.dp))

            if (isAnonymous) {
                StandardButton(
                    label = R.string.sign_in,
                    onButtonClick = {
                        openSignInScreen()
                    }
                )
            } else {
                userEmail?.let {
                    Text(
                        text = "Signed in as: $it",
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }

                StandardButton(
                    label = R.string.sign_out,
                    onButtonClick = {
                        signOut()
                    }
                )

                Spacer(Modifier.size(24.dp))

                // ─── Linked Accounts Section ─────────────────────────
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.size(16.dp))

                Text(
                    text = stringResource(R.string.linked_accounts),
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (linkedAccounts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_linked_accounts),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    linkedAccounts.forEach { account ->
                        LinkedAccountItem(
                            account = account,
                            onUnlink = { onUnlinkAccount(account.itemId) }
                        )
                    }
                }

                Spacer(Modifier.size(8.dp))

                StandardButton(
                    label = R.string.link_bank_account,
                    onButtonClick = onLinkBankAccount
                )

                Spacer(Modifier.size(8.dp))

                if (linkedAccounts.isNotEmpty()) {
                    if (isSyncing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = stringResource(R.string.syncing_transactions),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        StandardButton(
                            label = R.string.sync_transactions,
                            onButtonClick = onSyncTransactions
                        )
                    }
                }

                Spacer(Modifier.size(24.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.size(16.dp))

                DeleteAccountButton(deleteAccount)
            }
        }
    }
}

@Composable
fun LinkedAccountItem(
    account: LinkedAccount,
    onUnlink: () -> Unit
) {
    var showUnlinkDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = account.institutionName.ifBlank { "Linked Bank" },
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )
            IconButton(onClick = { showUnlinkDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.unlink_account)
                )
            }
        }
    }

    if (showUnlinkDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(stringResource(R.string.unlink_account_title)) },
            text = { Text(stringResource(R.string.unlink_account_description)) },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text(text = stringResource(R.string.cancel), fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkDialog = false
                        onUnlink()
                    },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.unlink), fontSize = 16.sp)
                }
            },
            onDismissRequest = { showUnlinkDialog = false }
        )
    }
}

@Composable
fun DeleteAccountButton(deleteAccount: () -> Unit) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    StandardButton(
        label = R.string.delete_account,
        onButtonClick = {
            showDeleteAccountDialog = true
        }
    )

    if (showDeleteAccountDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_description)) },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.cancel), fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccount()
                    },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.delete), fontSize = 16.sp)
                }
            },
            onDismissRequest = { showDeleteAccountDialog = false }
        )
    }
}

@Composable
private fun getDialogButtonColors(): ButtonColors {
    return ButtonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
        disabledContentColor = MaterialTheme.colorScheme.onErrorContainer
    )
}

@Composable
@Preview(showSystemUi = true)
fun SettingsScreenPreview() {
    FinanceTrackerTheme {
        SettingsScreenContent(
            loadCurrentUser = {},
            loadLinkedAccounts = {},
            openSignInScreen = {},
            signOut = {},
            deleteAccount = {},
            isAnonymous = false,
            userEmail = "jon.doe@email.com",
            linkedAccounts = listOf(
                LinkedAccount(itemId = "1", institutionName = "Chase"),
                LinkedAccount(itemId = "2", institutionName = "Bank of America")
            ),
            onLinkBankAccount = {},
            onSyncTransactions = {},
            onUnlinkAccount = {},
            isSyncing = false,
            snackbarHostState = SnackbarHostState()
        )
    }
}
