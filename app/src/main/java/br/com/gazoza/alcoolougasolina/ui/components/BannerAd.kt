package br.com.gazoza.alcoolougasolina.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import br.com.gazoza.alcoolougasolina.util.isDebug
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = if (isDebug()) {
        "ca-app-pub-3940256099942544/6300978111"
    } else {
        "ca-app-pub-6521704558504566/6221190272"
    },
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
