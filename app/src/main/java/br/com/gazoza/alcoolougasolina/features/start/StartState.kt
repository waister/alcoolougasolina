package br.com.gazoza.alcoolougasolina.features.start

data class StartUiState(val isLoading: Boolean = true)

sealed class StartEvent {
    data class NavigateToMain(val type: String = "") : StartEvent()

    object NavigateToNotifications : StartEvent()

    data class NavigateToNotificationDetails(val itemId: String) : StartEvent()
}
