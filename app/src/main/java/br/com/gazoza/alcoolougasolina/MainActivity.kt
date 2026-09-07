package br.com.gazoza.alcoolougasolina

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import br.com.gazoza.alcoolougasolina.navigation.AppNavHost
import br.com.gazoza.alcoolougasolina.ui.theme.AppTheme
import br.com.gazoza.alcoolougasolina.util.PARAM_ID
import br.com.gazoza.alcoolougasolina.util.PARAM_ITEM_ID
import br.com.gazoza.alcoolougasolina.util.PARAM_TYPE

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(PARAM_ID) ?: ""
        val type = intent.getStringExtra(PARAM_TYPE) ?: ""
        val itemId = intent.getStringExtra(PARAM_ITEM_ID) ?: ""

        setContent {
            AppTheme {
                AppNavHost(
                    initId = id,
                    initType = type,
                    initItemId = itemId,
                )
            }
        }
    }
}
