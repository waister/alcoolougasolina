package br.com.gazoza.alcoolougasolina.features.history

import br.com.gazoza.alcoolougasolina.domain.Comparison

data class HistoryUiState(val isLoading: Boolean = true, val comparisons: List<Comparison> = emptyList())

sealed class HistoryEvent {
    object ShowClearConfirmationDialog : HistoryEvent()

    object HistoryCleared : HistoryEvent()
}
