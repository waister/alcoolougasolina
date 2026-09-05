package br.com.gazoza.alcoolougasolina.features.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.ui.theme.AppTheme
import br.com.gazoza.alcoolougasolina.ui.theme.DarkBackground
import br.com.gazoza.alcoolougasolina.ui.theme.GreenPrimary
import org.koin.androidx.compose.koinViewModel

@Composable
fun StartScreen(
    id: String = "",
    type: String = "",
    itemId: String = "",
    onNavigateToMain: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToNotificationDetails: (String) -> Unit,
    viewModel: StartViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StartEvent.NavigateToMain -> onNavigateToMain(event.type)
                is StartEvent.NavigateToNotifications -> onNavigateToNotifications()
                is StartEvent.NavigateToNotificationDetails -> onNavigateToNotificationDetails(event.itemId)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initApp(id = id, type = type, itemId = itemId)
    }

    StartContent()
}

@Composable
fun StartContent() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(DarkBackground),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_128dp),
            contentDescription = "Logo",
            modifier = Modifier.size(128.dp),
        )

        CircularProgressIndicator(
            color = GreenPrimary,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
                    .size(36.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StartScreenPreview() {
    AppTheme {
        StartContent()
    }
}
