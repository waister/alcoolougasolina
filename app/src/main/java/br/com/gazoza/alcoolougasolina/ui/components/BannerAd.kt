package br.com.gazoza.alcoolougasolina.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import br.com.gazoza.alcoolougasolina.ui.theme.DarkBackground
import br.com.gazoza.alcoolougasolina.ui.theme.TextMuted
import br.com.gazoza.alcoolougasolina.util.isDebug
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String =
        if (isDebug()) {
            "ca-app-pub-3940256099942544/6300978111"
        } else {
            "ca-app-pub-6521704558504566/6221190272"
        },
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(DarkBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text("Banner Ad", color = TextMuted)
        }
        return
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(DarkBackground)
                .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}
