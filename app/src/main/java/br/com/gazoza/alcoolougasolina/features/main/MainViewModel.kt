package br.com.gazoza.alcoolougasolina.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gazoza.alcoolougasolina.BuildConfig
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.data.repository.HistoryRepository
import br.com.gazoza.alcoolougasolina.data.repository.NotificationRepository
import br.com.gazoza.alcoolougasolina.data.repository.PreferencesRepository
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.util.API_APP_NAME
import br.com.gazoza.alcoolougasolina.util.API_SHARE_LINK
import br.com.gazoza.alcoolougasolina.util.API_SUCCESS
import br.com.gazoza.alcoolougasolina.util.API_VERSION_MIN
import br.com.gazoza.alcoolougasolina.util.MaskMoney
import br.com.gazoza.alcoolougasolina.util.getBooleanVal
import br.com.gazoza.alcoolougasolina.util.getIntVal
import br.com.gazoza.alcoolougasolina.util.getStringVal
import java.text.DecimalFormat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val historyRepository: HistoryRepository,
    private val preferencesRepository: PreferencesRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val events: SharedFlow<MainEvent> = _events.asSharedFlow()

    init {
        loadInitialData()
        identifyUser()
    }

    private fun loadInitialData() {
        val lastEthanol = preferencesRepository.getLastEthanolPrice()
        val lastGasoline = preferencesRepository.getLastGasolinePrice()
        val shareLink = preferencesRepository.getShareLink()
        val appName = preferencesRepository.getAppName()

        _uiState.update {
            it.copy(
                priceEthanol = lastEthanol,
                priceGasoline = lastGasoline,
                shareLink = shareLink,
                appName = appName,
                isCalculateEnabled = lastEthanol.isNotEmpty() && lastGasoline.isNotEmpty(),
                isClearEnabled = lastEthanol.isNotEmpty() || lastGasoline.isNotEmpty(),
            )
        }
    }

    fun onEthanolPriceChanged(rawInput: String) {
        val formatted = formatPrice(rawInput)
        _uiState.update { current ->
            val canCalculate = formatted.isNotEmpty() && current.priceGasoline.isNotEmpty()
            val canClear = formatted.isNotEmpty() || current.priceGasoline.isNotEmpty()
            current.copy(
                priceEthanol = formatted,
                isCalculateEnabled = canCalculate,
                isClearEnabled = canClear,
                isResultVisible = false,
            )
        }
    }

    fun onGasolinePriceChanged(rawInput: String) {
        val formatted = formatPrice(rawInput)
        _uiState.update { current ->
            val canCalculate = current.priceEthanol.isNotEmpty() && formatted.isNotEmpty()
            val canClear = current.priceEthanol.isNotEmpty() || formatted.isNotEmpty()
            current.copy(
                priceGasoline = formatted,
                isCalculateEnabled = canCalculate,
                isClearEnabled = canClear,
                isResultVisible = false,
            )
        }
    }

    fun calculate() {
        val ethanolDouble = parsePrice(_uiState.value.priceEthanol)
        val gasolineDouble = parsePrice(_uiState.value.priceGasoline)

        if (ethanolDouble == 0.0) {
            viewModelScope.launch { _events.emit(MainEvent.ShowMessage(R.string.msg_require_ethanol)) }
            return
        }

        if (gasolineDouble == 0.0) {
            viewModelScope.launch { _events.emit(MainEvent.ShowMessage(R.string.msg_require_gasoline)) }
            return
        }

        val proportion = ethanolDouble / gasolineDouble
        val percentage = DecimalFormat("#.##").format(proportion * 100) + "%"

        val recommendation =
            if (proportion < 0.7) {
                FuelRecommendation.ETHANOL
            } else {
                FuelRecommendation.GASOLINE
            }

        val messageRes =
            if (proportion < 0.7) {
                R.string.msg_use_ethanol
            } else {
                R.string.msg_use_gasoline
            }

        _uiState.update {
            it.copy(
                recommendation = recommendation,
                proportion = proportion,
                percentageText = percentage,
                messageRes = messageRes,
                isResultVisible = true,
            )
        }

        preferencesRepository.setLastEthanolPrice(_uiState.value.priceEthanol)
        preferencesRepository.setLastGasolinePrice(_uiState.value.priceGasoline)

        viewModelScope.launch {
            val textEthanol = _uiState.value.priceEthanol
            val textGasoline = _uiState.value.priceGasoline
            var existing = historyRepository.getComparisonByPrices(textEthanol, textGasoline)
            if (existing == null) {
                existing =
                    Comparison(
                        priceEthanol = textEthanol,
                        priceGasoline = textGasoline,
                        proportion = proportion,
                        percentage = percentage,
                        timestamp = System.currentTimeMillis(),
                    )
            } else {
                existing.proportion = proportion
                existing.percentage = percentage
                existing.timestamp = System.currentTimeMillis()
            }
            historyRepository.insertOrUpdate(existing)
        }
    }

    fun clearInputs() {
        _uiState.update {
            it.copy(
                priceEthanol = "",
                priceGasoline = "",
                recommendation = null,
                proportion = 0.0,
                percentageText = "",
                messageRes = null,
                isCalculateEnabled = false,
                isClearEnabled = false,
                isResultVisible = false,
            )
        }
        preferencesRepository.setLastEthanolPrice("")
        preferencesRepository.setLastGasolinePrice("")
    }

    fun onShareClicked() {
        val shareLink = _uiState.value.shareLink
        val appName = _uiState.value.appName
        val text =
            if (shareLink.isNotEmpty()) {
                "$appName\n$shareLink"
            } else {
                appName
            }
        viewModelScope.launch {
            _events.emit(MainEvent.ShareApp(text))
        }
    }

    private fun identifyUser() {
        viewModelScope.launch {
            val token = preferencesRepository.getFcmToken()
            when (val result = notificationRepository.identifyUser(token)) {
                is br.com.gazoza.alcoolougasolina.data.repository.DataResult.Success -> {
                    val apiObj = result.data
                    if (apiObj.getBooleanVal(API_SUCCESS)) {
                        val shareLink = apiObj.getStringVal(API_SHARE_LINK)
                        val appName = apiObj.getStringVal(API_APP_NAME)
                        val versionMin = apiObj.getIntVal(API_VERSION_MIN)

                        if (shareLink.isNotEmpty()) {
                            preferencesRepository.setShareLink(shareLink)
                        }
                        if (appName.isNotEmpty()) {
                            preferencesRepository.setAppName(appName)
                        }

                        _uiState.update {
                            it.copy(
                                shareLink = shareLink,
                                appName = appName,
                            )
                        }

                        if (versionMin > 0 && BuildConfig.VERSION_CODE < versionMin) {
                            _events.emit(MainEvent.ShowUpdateDialog(shareLink))
                        }
                    }
                }

                is br.com.gazoza.alcoolougasolina.data.repository.DataResult.Error -> {}
            }
        }
    }

    private fun formatPrice(input: String): String = MaskMoney.format(input)

    private fun parsePrice(formatted: String): Double = MaskMoney.parse(formatted)
}
