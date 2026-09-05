package br.com.gazoza.alcoolougasolina.features.notifications

import br.com.gazoza.alcoolougasolina.data.repository.DataResult
import br.com.gazoza.alcoolougasolina.data.repository.NotificationRepository
import br.com.gazoza.alcoolougasolina.domain.NotificationItem
import br.com.gazoza.alcoolougasolina.utils.BaseRobolectricTest
import br.com.gazoza.alcoolougasolina.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest : BaseRobolectricTest() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val notificationRepository: NotificationRepository = mockk()
    private lateinit var viewModel: NotificationsViewModel

    private val mockNotificationList = listOf(
        NotificationItem(
            id = "1",
            title = "Promoção",
            body = "Corpo da notificação",
            date = "2026-09-05 10:00:00",
        ),
    )

    @Test
    fun `given success response, when initialized, then state contains notifications`() = runTest {
        coEvery { notificationRepository.getNotifications() } returns DataResult.Success(mockNotificationList)

        viewModel = NotificationsViewModel(notificationRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.notifications.size)
        assertEquals("Promoção", state.notifications[0].title)
        assertNull(state.errorMessage)
    }

    @Test
    fun `given failure response, when initialized, then state contains error message`() = runTest {
        coEvery { notificationRepository.getNotifications() } returns DataResult.Error("Network error")

        viewModel = NotificationsViewModel(notificationRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.notifications.size)
        assertEquals("Network error", state.errorMessage)
    }

    @Test
    fun `given notification id, when loadNotificationDetail is called, then loads selected notification`() = runTest {
        val detailItem = NotificationItem(id = "1", title = "Detalhe", body = "Texto")
        coEvery { notificationRepository.getNotifications() } returns DataResult.Success(emptyList())
        coEvery { notificationRepository.getNotificationDetail("1") } returns DataResult.Success(detailItem)

        viewModel = NotificationsViewModel(notificationRepository)
        viewModel.loadNotificationDetail("1")

        val state = viewModel.uiState.value
        assertNotNull(state.selectedNotification)
        assertEquals("Detalhe", state.selectedNotification?.title)
    }
}
