package br.com.gazoza.alcoolougasolina.features.main

import app.cash.turbine.test
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.data.repository.HistoryRepository
import br.com.gazoza.alcoolougasolina.data.repository.NotificationRepository
import br.com.gazoza.alcoolougasolina.data.repository.PreferencesRepository
import br.com.gazoza.alcoolougasolina.utils.BaseRobolectricTest
import br.com.gazoza.alcoolougasolina.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest : BaseRobolectricTest() {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val historyRepository: HistoryRepository = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)
    private val notificationRepository: NotificationRepository = mockk(relaxed = true)

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        every { preferencesRepository.getLastEthanolPrice() } returns ""
        every { preferencesRepository.getLastGasolinePrice() } returns ""
        every { preferencesRepository.getShareLink() } returns "https://maggapps.com"
        every { preferencesRepository.getAppName() } returns "Alcool ou Gasolina"
        every { preferencesRepository.getFcmToken() } returns "mock-token"
        coEvery { notificationRepository.identifyUser(any()) } returns
            br.com.gazoza.alcoolougasolina.data.repository.DataResult
                .Success(JSONObject())

        viewModel =
            MainViewModel(
                historyRepository = historyRepository,
                preferencesRepository = preferencesRepository,
                notificationRepository = notificationRepository
            )
    }

    @Test
    fun `given initial state, then state reflects preferences and empty prices`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.priceEthanol)
        assertEquals("", state.priceGasoline)
        assertFalse(state.isCalculateEnabled)
        assertFalse(state.isClearEnabled)
        assertFalse(state.isResultVisible)
    }

    @Test
    fun `given ethanol and gasoline prices, when price is updated, then calculate becomes enabled`() = runTest {
        viewModel.onEthanolPriceChanged("350")
        viewModel.onGasolinePriceChanged("550")

        val state = viewModel.uiState.value
        assertTrue(state.isCalculateEnabled)
        assertTrue(state.isClearEnabled)
    }

    @Test
    fun `given ethanol price ratio below 70 percent, when calculated, then recommends ethanol`() = runTest {
        viewModel.onEthanolPriceChanged("300")
        viewModel.onGasolinePriceChanged("500")

        viewModel.calculate()

        val state = viewModel.uiState.value
        assertEquals(FuelRecommendation.ETHANOL, state.recommendation)
        assertEquals(R.string.msg_use_ethanol, state.messageRes)
        assertTrue(state.isResultVisible)
        coVerify { historyRepository.insertOrUpdate(any()) }
        verify { preferencesRepository.setLastEthanolPrice(any()) }
        verify { preferencesRepository.setLastGasolinePrice(any()) }
    }

    @Test
    fun `given ethanol price ratio above 70 percent, when calculated, then recommends gasoline`() = runTest {
        viewModel.onEthanolPriceChanged("450")
        viewModel.onGasolinePriceChanged("500")

        viewModel.calculate()

        val state = viewModel.uiState.value
        assertEquals(FuelRecommendation.GASOLINE, state.recommendation)
        assertEquals(R.string.msg_use_gasoline, state.messageRes)
        assertTrue(state.isResultVisible)
    }

    @Test
    fun `given empty ethanol price, when calculated, then emits error event`() = runTest {
        viewModel.events.test {
            viewModel.calculate()
            val event = awaitItem()
            assertTrue(event is MainEvent.ShowMessage)
            assertEquals(R.string.msg_require_ethanol, (event as MainEvent.ShowMessage).messageRes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when clearInputs is called, then state and preferences are reset`() = runTest {
        viewModel.onEthanolPriceChanged("350")
        viewModel.onGasolinePriceChanged("550")
        viewModel.clearInputs()

        val state = viewModel.uiState.value
        assertEquals("", state.priceEthanol)
        assertEquals("", state.priceGasoline)
        assertNull(state.recommendation)
        assertFalse(state.isCalculateEnabled)
        assertFalse(state.isClearEnabled)
        verify { preferencesRepository.setLastEthanolPrice("") }
        verify { preferencesRepository.setLastGasolinePrice("") }
    }

    @Test
    fun `when onShareClicked is called, then emits ShareApp event`() = runTest {
        viewModel.events.test {
            viewModel.onShareClicked()
            val event = awaitItem()
            assertTrue(event is MainEvent.ShareApp)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
