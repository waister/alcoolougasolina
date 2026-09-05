package br.com.gazoza.alcoolougasolina.features.notifications

import br.com.gazoza.alcoolougasolina.domain.NotificationItem

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList(),
    val errorMessage: String? = null,
    val selectedNotification: NotificationItem? = null,
)

sealed class NotificationsEvent {
    data class OpenLink(val url: String) : NotificationsEvent()
    data class ShowError(val message: String) : NotificationsEvent()
}
