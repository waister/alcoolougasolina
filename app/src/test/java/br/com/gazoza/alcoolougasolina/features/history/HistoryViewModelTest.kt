package br.com.gazoza.alcoolougasolina.features.history

import app.cash.turbine.test
import br.com.gazoza.alcoolougasolina.data.repository.HistoryRepository
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import br.com.gazoza.alcoolougasolina.utils.BaseRobolectricTest

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest : BaseRobolectricTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val historyRepository: HistoryRepository = mockk(relaxed = true)
    private lateinit var viewModel: HistoryViewModel

    private val mockComparisons = listOf(
        Comparison(
            id = 1,
            priceEthanol = "R$ 3,50",
            priceGasoline = "R$ 5,50",
            proportion = 0.63,
            percentage = "63.64%",
            timestamp = 1000L,
        ),
    )

    @Before
    fun setup() {
        every { historyRepository.getAllComparisons() } returns flowOf(mockComparisons)
        viewModel = HistoryViewModel(historyRepository)
    }

    @Test
    fun `given comparisons in repository, when initialized, then state contains comparisons`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.comparisons.size)
        assertEquals("R$ 3,50", state.comparisons[0].priceEthanol)
    }

    @Test
    fun `when onClearHistoryClicked is called, then emits ShowClearConfirmationDialog event`() = runTest {
        viewModel.events.test {
            viewModel.onClearHistoryClicked()
            val event = awaitItem()
            assertTrue(event is HistoryEvent.ShowClearConfirmationDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when clearHistory is called, then calls repository deleteAll and emits HistoryCleared`() = runTest {
        coEvery { historyRepository.deleteAll() } returns Unit

        viewModel.events.test {
            viewModel.clearHistory()
            val event = awaitItem()
            assertTrue(event is HistoryEvent.HistoryCleared)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { historyRepository.deleteAll() }
    }
}
