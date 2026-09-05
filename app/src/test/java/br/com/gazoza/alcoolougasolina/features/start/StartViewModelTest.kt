package br.com.gazoza.alcoolougasolina.features.start

import app.cash.turbine.test
import br.com.gazoza.alcoolougasolina.data.repository.PreferencesRepository
import br.com.gazoza.alcoolougasolina.util.API_NOTIFICATIONS
import br.com.gazoza.alcoolougasolina.utils.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import br.com.gazoza.alcoolougasolina.utils.BaseRobolectricTest

@OptIn(ExperimentalCoroutinesApi::class)
class StartViewModelTest : BaseRobolectricTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)
    private lateinit var viewModel: StartViewModel

    @Before
    fun setup() {
        every { preferencesRepository.getDeviceId() } returns "123456789012345678"
        viewModel = StartViewModel(preferencesRepository)
    }

    @Test
    fun `given empty device id, when initApp is called, then generates numeric device ID`() = runTest {
        every { preferencesRepository.getDeviceId() } returns ""

        viewModel.initApp()

        verify { preferencesRepository.setDeviceId(any()) }
    }

    @Test
    fun `given standard launch, when initApp is called, then emits NavigateToMain`() = runTest {
        viewModel.events.test {
            viewModel.initApp()
            val event = awaitItem()
            assertTrue(event is StartEvent.NavigateToMain)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given notification type without item id, when initApp is called, then emits NavigateToNotifications`() = runTest {
        viewModel.events.test {
            viewModel.initApp(type = API_NOTIFICATIONS)
            val event = awaitItem()
            assertTrue(event is StartEvent.NavigateToNotifications)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given notification type with item id, when initApp is called, then emits NavigateToNotificationDetails`() = runTest {
        viewModel.events.test {
            viewModel.initApp(type = API_NOTIFICATIONS, itemId = "42")
            val event = awaitItem()
            assertTrue(event is StartEvent.NavigateToNotificationDetails)
            assertEquals("42", (event as StartEvent.NavigateToNotificationDetails).itemId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
