package com.jonathan.financetracker.ui.categoryMapping

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.BudgetMapping
import com.jonathan.financetracker.ui.components.CenterTopAppBar
import com.jonathan.financetracker.ui.components.StandardButton
import kotlinx.serialization.Serializable

@Serializable
object CategoryMappingRoute

@Composable
fun CategoryMappingScreen(
    viewModel: CategoryMappingViewModel = hiltViewModel()
) {
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val mappings by viewModel.mappings.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(true) {
        viewModel.load()
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar("Mappings saved")
            viewModel.onSaveSuccessShown()
        }
    }

    CategoryMappingScreenContent(
        plaidCategories = viewModel.plaidCategories,
        budgets = budgets,
        mappings = mappings,
        isSaving = isSaving,
        onMappingChanged = viewModel::setMapping,
        onSave = viewModel::save,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryMappingScreenContent(
    plaidCategories: List<String>,
    budgets: List<Budget>,
    mappings: Map<String, BudgetMapping>,
    isSaving: Boolean,
    onMappingChanged: (String, Budget?) -> Unit,
    onSave: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.category_mapping),
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
            Spacer(Modifier.size(16.dp))

            Text(
                text = stringResource(R.string.category_mapping_description),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.size(8.dp))

            plaidCategories.forEach { plaidCategory ->
                val currentMapping = mappings[plaidCategory]
                CategoryMappingRow(
                    plaidCategory = plaidCategory,
                    budgets = budgets,
                    selectedBudgetName = currentMapping?.budgetName,
                    onBudgetSelected = { budget -> onMappingChanged(plaidCategory, budget) }
                )
            }

            Spacer(Modifier.size(16.dp))

            if (isSaving) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(12.dp))
                    Text(text = "Saving...", fontSize = 14.sp)
                }
            } else {
                StandardButton(
                    label = R.string.save_mappings,
                    onButtonClick = onSave
                )
            }

            Spacer(Modifier.size(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryMappingRow(
    plaidCategory: String,
    budgets: List<Budget>,
    selectedBudgetName: String?,
    onBudgetSelected: (Budget?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Text(
            text = plaidCategory,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedBudgetName ?: "None",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        onBudgetSelected(null)
                        expanded = false
                    }
                )
                budgets.forEach { budget ->
                    DropdownMenuItem(
                        text = { Text(budget.category) },
                        onClick = {
                            onBudgetSelected(budget)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
