package br.com.gazoza.alcoolougasolina.features.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.ui.components.AppTopBar
import br.com.gazoza.alcoolougasolina.ui.components.BannerAd
import br.com.gazoza.alcoolougasolina.ui.theme.AppTheme
import br.com.gazoza.alcoolougasolina.ui.theme.DarkBackground
import br.com.gazoza.alcoolougasolina.ui.theme.DarkCard
import br.com.gazoza.alcoolougasolina.ui.theme.GreenLight
import br.com.gazoza.alcoolougasolina.ui.theme.GreenPrimary
import br.com.gazoza.alcoolougasolina.ui.theme.TextMuted
import br.com.gazoza.alcoolougasolina.ui.theme.TextPrimary
import br.com.gazoza.alcoolougasolina.util.formatDatetime
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(onBackClick: () -> Unit, viewModel: HistoryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryEvent.ShowClearConfirmationDialog -> {
                    showConfirmDialog = true
                }

                is HistoryEvent.HistoryCleared -> {}
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.confirmation)) },
            text = { Text(stringResource(R.string.confirm_clear_history)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.clearHistory()
                    },
                ) {
                    Text(stringResource(R.string.clear_history), color = GreenLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    HistoryContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onClearHistoryClick = viewModel::onClearHistoryClicked,
    )
}

@Composable
fun HistoryContent(uiState: HistoryUiState, onBackClick: () -> Unit, onClearHistoryClick: () -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.history),
                onBackClick = onBackClick,
                actions = {
                    if (uiState.comparisons.isNotEmpty()) {
                        IconButton(onClick = onClearHistoryClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.clear_history),
                                tint = TextPrimary,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            BannerAd()
        },
        containerColor = DarkBackground,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = GreenPrimary,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.comparisons.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.history_empty),
                        color = TextMuted,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.comparisons, key = { it.id }) { comparison ->
                            HistoryItem(comparison = comparison)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(comparison: Comparison, modifier: Modifier = Modifier) {
    val isEthanol = comparison.proportion < 0.7
    val resultTextRes = if (isEthanol) R.string.msg_use_ethanol else R.string.msg_use_gasoline
    val iconRes = if (isEthanol) R.drawable.ic_ethanol_36dp else R.drawable.ic_gasoline_36dp

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comparison.timestamp.formatDatetime(),
                    color = TextMuted,
                    fontSize = 12.sp,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.label_ethanol, comparison.priceEthanol),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = " x ",
                        color = TextMuted,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = stringResource(R.string.label_gasoline, comparison.priceGasoline),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${stringResource(resultTextRes)} (${comparison.percentage})",
                    color = GreenLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Preview(name = "History Screen - Loaded", showBackground = true)
@Composable
private fun HistoryScreenLoadedPreview() {
    AppTheme {
        HistoryContent(
            uiState =
                HistoryUiState(
                    isLoading = false,
                    comparisons =
                        listOf(
                            Comparison(
                                id = 1,
                                priceEthanol = "R$ 3,28",
                                priceGasoline = "R$ 5,90",
                                proportion = 0.5559,
                                percentage = "55.59%",
                                timestamp = 1718000000000L,
                            ),
                            Comparison(
                                id = 2,
                                priceEthanol = "R$ 4,80",
                                priceGasoline = "R$ 5,50",
                                proportion = 0.8727,
                                percentage = "87.27%",
                                timestamp = 1717900000000L,
                            ),
                        ),
                ),
            onBackClick = {},
            onClearHistoryClick = {},
        )
    }
}

@Preview(name = "History Screen - Empty", showBackground = true)
@Composable
private fun HistoryScreenEmptyPreview() {
    AppTheme {
        HistoryContent(
            uiState = HistoryUiState(isLoading = false, comparisons = emptyList()),
            onBackClick = {},
            onClearHistoryClick = {},
        )
    }
}

@Preview(name = "History Screen - Loading", showBackground = true)
@Composable
private fun HistoryScreenLoadingPreview() {
    AppTheme {
        HistoryContent(
            uiState = HistoryUiState(isLoading = true, comparisons = emptyList()),
            onBackClick = {},
            onClearHistoryClick = {},
        )
    }
}
