package br.com.gazoza.alcoolougasolina.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gazoza.alcoolougasolina.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotificationsEvent>()
    val events: SharedFlow<NotificationsEvent> = _events.asSharedFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = notificationRepository.getNotifications()) {
                is br.com.gazoza.alcoolougasolina.data.repository.DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = result.data,
                            errorMessage = null
                        )
                    }
                }

                is br.com.gazoza.alcoolougasolina.data.repository.DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadNotificationDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = notificationRepository.getNotificationDetail(id)) {
                is br.com.gazoza.alcoolougasolina.data.repository.DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedNotification = result.data,
                            errorMessage = null
                        )
                    }
                }

                is br.com.gazoza.alcoolougasolina.data.repository.DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    _events.emit(NotificationsEvent.ShowError(result.message))
                }
            }
        }
    }
}
