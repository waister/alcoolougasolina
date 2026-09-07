package br.com.gazoza.alcoolougasolina.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gazoza.alcoolougasolina.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyRepository: HistoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HistoryEvent>()
    val events: SharedFlow<HistoryEvent> = _events.asSharedFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getAllComparisons().collect { comparisons ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        comparisons = comparisons,
                    )
                }
            }
        }
    }

    fun onClearHistoryClicked() {
        viewModelScope.launch {
            _events.emit(HistoryEvent.ShowClearConfirmationDialog)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.deleteAll()
            _events.emit(HistoryEvent.HistoryCleared)
        }
    }
}
