package br.com.gazoza.alcoolougasolina.features.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gazoza.alcoolougasolina.data.repository.PreferencesRepository
import br.com.gazoza.alcoolougasolina.util.API_NOTIFICATIONS
import br.com.gazoza.alcoolougasolina.util.isNotNumeric
import br.com.gazoza.alcoolougasolina.util.sendNotificationReport
import java.util.Calendar
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StartUiState())
    val uiState: StateFlow<StartUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StartEvent>()
    val events: SharedFlow<StartEvent> = _events.asSharedFlow()

    fun initApp(id: String = "", type: String = "", itemId: String = "") {
        viewModelScope.launch {
            ensureDeviceId()

            if (id.isNotEmpty()) {
                sendNotificationReport(id, false)
            }

            if (type == API_NOTIFICATIONS) {
                if (itemId.isEmpty()) {
                    _events.emit(StartEvent.NavigateToNotifications)
                } else {
                    _events.emit(StartEvent.NavigateToNotificationDetails(itemId))
                }
            } else {
                _events.emit(StartEvent.NavigateToMain(type))
            }
        }
    }

    private fun ensureDeviceId() {
        val currentDeviceId = preferencesRepository.getDeviceId()
        val isNotNumeric = currentDeviceId.isNotNumeric()

        if (currentDeviceId.isEmpty() || isNotNumeric) {
            if (isNotNumeric) {
                preferencesRepository.setDeviceIdOld(currentDeviceId)
            }

            val milliseconds = Calendar.getInstance().timeInMillis.toString()
            val random = Random.nextInt(10000, 99999)
            var stringId = "$milliseconds$random"

            if (stringId.length > 18) {
                stringId = stringId.substring(0, 18)
            } else if (stringId.length < 18) {
                stringId = stringId.padEnd(18, '9')
            }

            preferencesRepository.setDeviceId(stringId)
        }
    }
}
