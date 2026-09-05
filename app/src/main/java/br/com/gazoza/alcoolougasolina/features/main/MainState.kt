package br.com.gazoza.alcoolougasolina.features.main

enum class FuelRecommendation {
    ETHANOL,
    GASOLINE
}

data class MainUiState(
    val priceEthanol: String = "",
    val priceGasoline: String = "",
    val recommendation: FuelRecommendation? = null,
    val proportion: Double = 0.0,
    val percentageText: String = "",
    val messageRes: Int? = null,
    val isCalculateEnabled: Boolean = false,
    val isClearEnabled: Boolean = false,
    val isResultVisible: Boolean = false,
    val shareLink: String = "",
    val appName: String = ""
)

sealed class MainEvent {
    data class ShowMessage(val messageRes: Int) : MainEvent()

    data class ShareApp(val shareText: String) : MainEvent()

    data class OpenUrl(val url: String) : MainEvent()

    object NavigateToHistory : MainEvent()

    object NavigateToNotifications : MainEvent()

    data class ShowUpdateDialog(val storeLink: String) : MainEvent()
}
