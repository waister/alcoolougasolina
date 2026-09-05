package br.com.gazoza.alcoolougasolina.features.notifications

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.domain.NotificationItem
import br.com.gazoza.alcoolougasolina.ui.components.AppTopBar
import br.com.gazoza.alcoolougasolina.ui.components.BannerAd
import br.com.gazoza.alcoolougasolina.ui.theme.DarkBackground
import br.com.gazoza.alcoolougasolina.ui.theme.DarkCard
import br.com.gazoza.alcoolougasolina.ui.theme.GreenPrimary
import br.com.gazoza.alcoolougasolina.ui.theme.TextMuted
import br.com.gazoza.alcoolougasolina.ui.theme.TextPrimary
import br.com.gazoza.alcoolougasolina.ui.theme.TextSecondary
import br.com.gazoza.alcoolougasolina.util.formatDatetime
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.notifications),
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
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage!!,
                        color = TextMuted,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                    )
                }
                uiState.notifications.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.notifications_empty),
                        color = TextMuted,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(uiState.notifications, key = { it.id }) { item ->
                            NotificationRow(
                                item = item,
                                onClick = { onNotificationClick(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (item.date.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.date.formatDatetime(),
                        color = TextMuted,
                        fontSize = 11.sp,
                    )
                }
            }

            if (item.body.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.body,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
