package com.jonathan.financetracker.ui.Transactions

import app.cash.turbine.test
import com.jonathan.financetracker.MainDispatcherRule
import com.jonathan.financetracker.data.model.Transaction
import com.jonathan.financetracker.data.repository.AuthRepository
import com.jonathan.financetracker.data.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>()
    private val transactionRepository = mockk<TransactionRepository>()
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setup() {
        // Default: auth returns a user ID, repository returns empty list
        every { authRepository.currentUserIdFlow } returns flowOf("test-user-id")
        every {
            transactionRepository.getMonthlyTransactions(
                currentUserIdFlow = any(),
                yearMonth = any()
            )
        } returns flowOf(emptyList())
    }

    private fun createViewModel(): TransactionsViewModel {
        return TransactionsViewModel(authRepository, transactionRepository)
    }

    // --- selectedMonth tests ---

    @Test
    fun `initial selected month is current month`() {
        viewModel = createViewModel()

        assertEquals(YearMonth.now(), viewModel.selectedMonth.value)
    }

    @Test
    fun `goToPreviousMonth decrements the month by one`() {
        viewModel = createViewModel()
        val initialMonth = viewModel.selectedMonth.value

        viewModel.goToPreviousMonth()

        assertEquals(initialMonth.minusMonths(1), viewModel.selectedMonth.value)
    }

    @Test
    fun `goToNextMonth increments the month by one`() {
        viewModel = createViewModel()
        // First go back so we can go forward
        viewModel.goToPreviousMonth()
        val monthAfterGoingBack = viewModel.selectedMonth.value

        viewModel.goToNextMonth()

        assertEquals(monthAfterGoingBack.plusMonths(1), viewModel.selectedMonth.value)
    }

    @Test
    fun `goToPreviousMonth multiple times goes back multiple months`() {
        viewModel = createViewModel()
        val initialMonth = viewModel.selectedMonth.value

        viewModel.goToPreviousMonth()
        viewModel.goToPreviousMonth()
        viewModel.goToPreviousMonth()

        assertEquals(initialMonth.minusMonths(3), viewModel.selectedMonth.value)
    }

    // --- canGoToNextMonth tests ---

    @Test
    fun `canGoToNextMonth returns false when on current month`() {
        viewModel = createViewModel()

        assertFalse(viewModel.canGoToNextMonth())
    }

    @Test
    fun `canGoToNextMonth returns true when on a past month`() {
        viewModel = createViewModel()
        viewModel.goToPreviousMonth()

        assertTrue(viewModel.canGoToNextMonth())
    }

    @Test
    fun `canGoToNextMonth returns false after navigating back to current month`() {
        viewModel = createViewModel()
        viewModel.goToPreviousMonth()
        viewModel.goToNextMonth()

        assertFalse(viewModel.canGoToNextMonth())
    }

    // --- transactions flow tests ---

    @Test
    fun `transactions starts with empty list`() = runTest {
        viewModel = createViewModel()

        viewModel.transactions.test {
            assertEquals(emptyList<Transaction>(), awaitItem())
        }
    }

    @Test
    fun `transactions emits data from repository`() = runTest {
        val testTransactions = listOf(
            Transaction(id = "1", description = "Groceries", amount = 50.0),
            Transaction(id = "2", description = "Gas", amount = 30.0)
        )
        every {
            transactionRepository.getMonthlyTransactions(
                currentUserIdFlow = any(),
                yearMonth = any()
            )
        } returns flowOf(testTransactions)

        viewModel = createViewModel()

        viewModel.transactions.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Groceries", result[0].description)
            assertEquals(50.0, result[0].amount, 0.001)
            assertEquals("Gas", result[1].description)
            assertEquals(30.0, result[1].amount, 0.001)
        }
    }

    @Test
    fun `transactions emits empty list when user is not logged in`() = runTest {
        every { authRepository.currentUserIdFlow } returns flowOf(null)
        every {
            transactionRepository.getMonthlyTransactions(
                currentUserIdFlow = any(),
                yearMonth = any()
            )
        } returns flowOf(emptyList())

        viewModel = createViewModel()

        viewModel.transactions.test {
            assertEquals(emptyList<Transaction>(), awaitItem())
        }
    }

    // --- month navigation + transactions integration ---

    @Test
    fun `changing month triggers new transaction fetch`() = runTest {
        val currentMonthTransactions = listOf(
            Transaction(id = "1", description = "March item", amount = 10.0)
        )
        val lastMonthTransactions = listOf(
            Transaction(id = "2", description = "February item", amount = 20.0)
        )

        val transactionsFlow = MutableStateFlow(currentMonthTransactions)

        every {
            transactionRepository.getMonthlyTransactions(
                currentUserIdFlow = any(),
                yearMonth = any()
            )
        } returns transactionsFlow

        viewModel = createViewModel()

        viewModel.transactions.test {
            // Initial emission - current month
            assertEquals("March item", awaitItem()[0].description)

            // Simulate what the repository would return for previous month
            transactionsFlow.value = lastMonthTransactions
            viewModel.goToPreviousMonth()

            assertEquals("February item", awaitItem()[0].description)
        }
    }
}
