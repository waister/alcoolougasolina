package br.com.gazoza.alcoolougasolina.features.notifications

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.ui.components.AppTopBar
import br.com.gazoza.alcoolougasolina.ui.components.BannerAd
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
            modifier = Modifier
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
                uiState.selectedNotification != null -> {
                    val notif = uiState.selectedNotification!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        if (notif.image.isNotEmpty()) {
                            AsyncImage(
                                model = notif.image,
                                contentDescription = stringResource(R.string.notification_image),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
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
                                text = stringResource(R.string.label_received, notif.date.formatDatetime()),
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
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notif.link))
                                    context.startActivity(intent)
                                },
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
