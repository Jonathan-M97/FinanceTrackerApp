package com.jonathan.financetracker.ui.addtransaction

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.jonathan.financetracker.MainDispatcherRule
import com.jonathan.financetracker.R
import com.jonathan.financetracker.data.model.Budget
import com.jonathan.financetracker.data.model.ErrorMessage
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.BudgetRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>()
    private val transactionRepository = mockk<TransactionRepository>()
    private val budgetRepository = mockk<BudgetRepository>()
    private val firebaseUser = mockk<FirebaseUser>()

    @Before
    fun setup() {
        every { firebaseUser.uid } returns "test-user-id"
        every { authRepository.currentUser } returns firebaseUser
        every { authRepository.currentUserIdFlow } returns flowOf("test-user-id")
        every { budgetRepository.getBudgets(any()) } returns flowOf(emptyList())
    }

    private fun createViewModel(itemId: String = ""): AddTransactionViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("itemId" to itemId))
        return AddTransactionViewModel(
            savedStateHandle = savedStateHandle,
            authRepository = authRepository,
            transactionRepository = transactionRepository,
            budgetsRepository = budgetRepository
        )
    }

    // --- loadItem tests ---

    @Test
    fun `loadItem sets new Transaction when itemId is blank`() = runTest {
        val viewModel = createViewModel(itemId = "")

        viewModel.transactionItem.test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals("", item!!.description)
            assertEquals(0.0, item.amount, 0.001)
        }
    }

    @Test
    fun `loadItem fetches transaction from repository when itemId is not blank`() = runTest {
        val existingTransaction = Transaction(
            id = "tx-123",
            description = "Existing item",
            amount = 42.0
        )
        coEvery { transactionRepository.getTransaction("tx-123") } returns existingTransaction

        val viewModel = createViewModel(itemId = "tx-123")

        viewModel.transactionItem.test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals("Existing item", item!!.description)
            assertEquals(42.0, item.amount, 0.001)
        }
    }

    // --- saveItem validation tests ---

    @Test
    fun `saveItem shows error when owner id is null`() {
        every { authRepository.currentUser } returns null

        val viewModel = createViewModel(itemId = "")
        var errorMessage: ErrorMessage? = null

        viewModel.saveItem(
            item = Transaction(description = "Test"),
            showErrorSnackbar = { errorMessage = it }
        )

        assertNotNull(errorMessage)
        assertTrue(errorMessage is ErrorMessage.IdError)
        assertEquals(
            R.string.error_missing_owner_id,
            (errorMessage as ErrorMessage.IdError).message
        )
    }

    @Test
    fun `saveItem shows error when description is blank`() {
        val viewModel = createViewModel(itemId = "")
        var errorMessage: ErrorMessage? = null

        viewModel.saveItem(
            item = Transaction(description = ""),
            showErrorSnackbar = { errorMessage = it }
        )

        assertNotNull(errorMessage)
        assertTrue(errorMessage is ErrorMessage.IdError)
        assertEquals(
            R.string.error_missing_description,
            (errorMessage as ErrorMessage.IdError).message
        )
    }

    // --- saveItem create/update tests ---

    @Test
    fun `saveItem creates new transaction when itemId is blank`() = runTest {
        coEvery { transactionRepository.create(any()) } returns "new-id"

        val viewModel = createViewModel(itemId = "")
        val transaction = Transaction(
            description = "New purchase",
            amount = 25.0,
            date = Timestamp.now()
        )

        viewModel.saveItem(item = transaction, showErrorSnackbar = {})

        coVerify {
            transactionRepository.create(match {
                it.description == "New purchase" && it.ownerId == "test-user-id"
            })
        }
    }

    @Test
    fun `saveItem updates existing transaction when itemId is not blank`() = runTest {
        val existing = Transaction(id = "tx-123", description = "Old", amount = 10.0)
        coEvery { transactionRepository.getTransaction("tx-123") } returns existing
        coEvery { transactionRepository.update(any()) } just Runs

        val viewModel = createViewModel(itemId = "tx-123")
        val updated = Transaction(
            id = "tx-123",
            description = "Updated purchase",
            amount = 30.0,
            date = Timestamp.now()
        )

        viewModel.saveItem(item = updated, showErrorSnackbar = {})

        coVerify {
            transactionRepository.update(match {
                it.description == "Updated purchase" && it.ownerId == "test-user-id"
            })
        }
    }

    @Test
    fun `saveItem sets navigateDashboard to true after successful create`() = runTest {
        coEvery { transactionRepository.create(any()) } returns "new-id"

        val viewModel = createViewModel(itemId = "")

        viewModel.navigateDashboard.test {
            assertEquals(false, awaitItem()) // initial value

            viewModel.saveItem(
                item = Transaction(description = "Test", date = Timestamp.now()),
                showErrorSnackbar = {}
            )

            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `saveItem populates yearMonth from transaction date`() = runTest {
        coEvery { transactionRepository.create(any()) } returns "new-id"

        val viewModel = createViewModel(itemId = "")
        val transaction = Transaction(
            description = "Test",
            amount = 10.0,
            date = Timestamp.now()
        )

        viewModel.saveItem(item = transaction, showErrorSnackbar = {})

        coVerify {
            transactionRepository.create(match {
                it.yearMonth.isNotBlank() && it.yearMonth.matches(Regex("\\d{4}-\\d{2}"))
            })
        }
    }

    // --- deleteItem tests ---

    @Test
    fun `deleteItem does nothing when itemId is blank`() = runTest {
        val viewModel = createViewModel(itemId = "")

        viewModel.deleteItem(
            item = Transaction(id = "some-id"),
            showErrorSnackbar = {}
        )

        coVerify(exactly = 0) { transactionRepository.delete(any()) }
    }

    @Test
    fun `deleteItem deletes transaction and navigates to dashboard`() = runTest {
        val existing = Transaction(id = "tx-123", description = "To delete", amount = 5.0)
        coEvery { transactionRepository.getTransaction("tx-123") } returns existing
        coEvery { transactionRepository.delete("tx-123") } just Runs

        val viewModel = createViewModel(itemId = "tx-123")

        viewModel.navigateDashboard.test {
            assertEquals(false, awaitItem())

            viewModel.deleteItem(
                item = Transaction(id = "tx-123"),
                showErrorSnackbar = {}
            )

            assertEquals(true, awaitItem())
        }

        coVerify { transactionRepository.delete("tx-123") }
    }

    @Test
    fun `deleteItem shows error when transaction id is null`() = runTest {
        val existing = Transaction(id = "tx-123", description = "Existing")
        coEvery { transactionRepository.getTransaction("tx-123") } returns existing

        val viewModel = createViewModel(itemId = "tx-123")
        var errorMessage: ErrorMessage? = null

        viewModel.deleteItem(
            item = Transaction(id = null),
            showErrorSnackbar = { errorMessage = it }
        )

        assertNotNull(errorMessage)
        assertTrue(errorMessage is ErrorMessage.IdError)
        assertEquals(
            R.string.error_missing_id,
            (errorMessage as ErrorMessage.IdError).message
        )
    }

    // --- budgetList tests ---

    @Test
    fun `budgetList starts with empty list`() = runTest {
        val viewModel = createViewModel(itemId = "")

        viewModel.budgetList.test {
            assertEquals(emptyList<BudgetUI>(), awaitItem())
        }
    }

    @Test
    fun `budgetList emits budgets from repository`() = runTest {
        val budgets = listOf(
            Budget(id = "b1", category = "Food"),
            Budget(id = "b2", category = "Transport")
        )
        every { budgetRepository.getBudgets(any()) } returns flowOf(budgets)

        val viewModel = createViewModel(itemId = "")

        viewModel.budgetList.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Food", result[0].name)
            assertEquals("b1", result[0].id)
            assertEquals("Transport", result[1].name)
            assertEquals("b2", result[1].id)
        }
    }
}
