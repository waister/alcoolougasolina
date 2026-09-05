package br.com.gazoza.alcoolougasolina.features.main

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.ui.components.AppTopBar
import br.com.gazoza.alcoolougasolina.ui.components.BannerAd
import br.com.gazoza.alcoolougasolina.ui.theme.DarkBackground
import br.com.gazoza.alcoolougasolina.ui.theme.DarkCard
import br.com.gazoza.alcoolougasolina.ui.theme.GreenLight
import br.com.gazoza.alcoolougasolina.ui.theme.GreenPrimary
import br.com.gazoza.alcoolougasolina.ui.theme.TextMuted
import br.com.gazoza.alcoolougasolina.ui.theme.TextPrimary
import br.com.gazoza.alcoolougasolina.ui.theme.TextSecondary
import br.com.gazoza.alcoolougasolina.util.MaskMoney
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: MainViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var updateUrlToOpen by remember { mutableStateOf<String?>(null) }

    var ethanolTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = uiState.priceEthanol,
                selection = TextRange(uiState.priceEthanol.length),
            ),
        )
    }
    var gasolineTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = uiState.priceGasoline,
                selection = TextRange(uiState.priceGasoline.length),
            ),
        )
    }

    LaunchedEffect(uiState.priceEthanol) {
        if (uiState.priceEthanol != ethanolTextFieldValue.text) {
            ethanolTextFieldValue = TextFieldValue(
                text = uiState.priceEthanol,
                selection = TextRange(uiState.priceEthanol.length),
            )
        }
    }

    LaunchedEffect(uiState.priceGasoline) {
        if (uiState.priceGasoline != gasolineTextFieldValue.text) {
            gasolineTextFieldValue = TextFieldValue(
                text = uiState.priceGasoline,
                selection = TextRange(uiState.priceGasoline.length),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainEvent.ShowMessage -> {
                    Toast.makeText(context, context.getString(event.messageRes), Toast.LENGTH_SHORT).show()
                }
                is MainEvent.ShareApp -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_app)))
                }
                is MainEvent.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    context.startActivity(intent)
                }
                is MainEvent.NavigateToHistory -> onNavigateToHistory()
                is MainEvent.NavigateToNotifications -> onNavigateToNotifications()
                is MainEvent.ShowUpdateDialog -> {
                    updateUrlToOpen = event.storeLink
                }
            }
        }
    }

    if (updateUrlToOpen != null) {
        AlertDialog(
            onDismissRequest = { updateUrlToOpen = null },
            title = { Text(stringResource(R.string.update_title)) },
            text = { Text(stringResource(R.string.update_available)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val url = updateUrlToOpen
                        updateUrlToOpen = null
                        if (!url.isNullOrEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    },
                ) {
                    Text(stringResource(R.string.update_positive))
                }
            },
            dismissButton = {
                TextButton(onClick = { updateUrlToOpen = null }) {
                    Text(stringResource(R.string.update_negative))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.notifications),
                            tint = TextPrimary,
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.history),
                            tint = TextPrimary,
                        )
                    }
                    IconButton(onClick = { viewModel.onShareClicked() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_app),
                            tint = TextPrimary,
                        )
                    }
                },
            )
        },
        bottomBar = {
            BannerAd()
        },
        containerColor = DarkBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_128dp),
                contentDescription = stringResource(R.string.logo_description),
                modifier = Modifier
                    .size(110.dp)
                    .padding(vertical = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Price Inputs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Ethanol Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ethanol),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = ethanolTextFieldValue,
                        onValueChange = { newValue ->
                            if (newValue.text == ethanolTextFieldValue.text) {
                                if (newValue.text.isNotEmpty() && newValue.selection != TextRange(0, newValue.text.length)) {
                                    ethanolTextFieldValue = newValue.copy(
                                        selection = TextRange(0, newValue.text.length),
                                    )
                                }
                            } else {
                                val formatted = MaskMoney.formatMoneyInput(
                                    previousText = ethanolTextFieldValue.text,
                                    newText = newValue.text,
                                )
                                ethanolTextFieldValue = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length),
                                )
                                viewModel.onEthanolPriceChanged(formatted)
                            }
                        },
                        placeholder = { Text("R$ 0,00", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GreenLight,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && ethanolTextFieldValue.text.isNotEmpty()) {
                                    ethanolTextFieldValue = ethanolTextFieldValue.copy(
                                        selection = TextRange(0, ethanolTextFieldValue.text.length),
                                    )
                                }
                            },
                    )
                }

                Text(
                    text = stringResource(R.string.separator),
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )

                // Gasoline Field
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.gasoline),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = gasolineTextFieldValue,
                        onValueChange = { newValue ->
                            if (newValue.text == gasolineTextFieldValue.text) {
                                if (newValue.text.isNotEmpty() && newValue.selection != TextRange(0, newValue.text.length)) {
                                    gasolineTextFieldValue = newValue.copy(
                                        selection = TextRange(0, newValue.text.length),
                                    )
                                }
                            } else {
                                val formatted = MaskMoney.formatMoneyInput(
                                    previousText = gasolineTextFieldValue.text,
                                    newText = newValue.text,
                                )
                                gasolineTextFieldValue = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length),
                                )
                                viewModel.onGasolinePriceChanged(formatted)
                            }
                        },
                        placeholder = { Text("R$ 0,00", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.calculate()
                            },
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GreenLight,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused && gasolineTextFieldValue.text.isNotEmpty()) {
                                    gasolineTextFieldValue = gasolineTextFieldValue.copy(
                                        selection = TextRange(0, gasolineTextFieldValue.text.length),
                                    )
                                }
                            },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Result Card
            if (uiState.isResultVisible && uiState.messageRes != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(uiState.messageRes!!),
                                color = GreenLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                            if (uiState.percentageText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.msg_result, uiState.percentageText),
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                )
                            }
                        }

                        val iconRes = if (uiState.recommendation == FuelRecommendation.ETHANOL) {
                            R.drawable.ic_ethanol_36dp
                        } else {
                            R.drawable.ic_gasoline_36dp
                        }
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Buttons
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.calculate()
                },
                enabled = uiState.isCalculateEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.4f),
                    contentColor = TextPrimary,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = stringResource(R.string.calculate).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            if (uiState.isClearEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.clearInputs()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        color = TextMuted,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}
