package br.com.gazoza.alcoolougasolina.features.notifications

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.domain.NotificationItem
import br.com.gazoza.alcoolougasolina.ui.components.AppTopBar
import br.com.gazoza.alcoolougasolina.ui.components.BannerAd
import br.com.gazoza.alcoolougasolina.ui.theme.AppTheme
import br.com.gazoza.alcoolougasolina.ui.theme.DarkBackground
import br.com.gazoza.alcoolougasolina.ui.theme.GreenPrimary
import br.com.gazoza.alcoolougasolina.ui.theme.TextMuted
import br.com.gazoza.alcoolougasolina.ui.theme.TextPrimary
import br.com.gazoza.alcoolougasolina.ui.theme.TextSecondary
import br.com.gazoza.alcoolougasolina.util.formatDatetime
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationDetailsScreen(
    notificationId: String,
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(notificationId) {
        viewModel.loadNotificationDetail(notificationId)
    }

    NotificationDetailsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onOpenLink = { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
    )
}

@Composable
fun NotificationDetailsContent(uiState: NotificationsUiState, onBackClick: () -> Unit, onOpenLink: (String) -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.notification_details),
                onBackClick = onBackClick,
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
            val notif = uiState.selectedNotification
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = GreenPrimary,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                notif != null -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                    ) {
                        if (notif.image.isNotEmpty()) {
                            AsyncImage(
                                model = notif.image,
                                contentDescription = stringResource(R.string.notification_image),
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )

                        if (notif.date.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text =
                                    stringResource(
                                        R.string.label_received,
                                        notif.date.formatDatetime(),
                                    ),
                                color = TextMuted,
                                fontSize = 12.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = notif.body,
                            color = TextSecondary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        )

                        if (notif.link.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { onOpenLink(notif.link) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = stringResource(R.string.label_link, ""),
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Notification Details - Loaded", showBackground = true)
@Composable
private fun NotificationDetailsLoadedPreview() {
    AppTheme {
        NotificationDetailsContent(
            uiState =
                NotificationsUiState(
                    isLoading = false,
                    selectedNotification =
                        NotificationItem(
                            id = "1",
                            title = "Preço dos combustíveis subiu!",
                            body =
                                "Confira a nova proporção calculada para abastecer com " +
                                    "economia nos postos da sua região.",
                            date = "2026-06-01 10:30:00",
                            link = "https://maggapps.com",
                        ),
                ),
            onBackClick = {},
            onOpenLink = {},
        )
    }
}

@Preview(name = "Notification Details - Loading", showBackground = true)
@Composable
private fun NotificationDetailsLoadingPreview() {
    AppTheme {
        NotificationDetailsContent(
            uiState =
                NotificationsUiState(
                    isLoading = true,
                    selectedNotification = null,
                ),
            onBackClick = {},
            onOpenLink = {},
        )
    }
}
