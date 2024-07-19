package br.com.gazoza.alcoolougasolina.application

import android.app.Application
import android.os.Build
import br.com.gazoza.alcoolougasolina.BuildConfig
import br.com.gazoza.alcoolougasolina.util.API_ANDROID
import br.com.gazoza.alcoolougasolina.util.API_DEBUG
import br.com.gazoza.alcoolougasolina.util.API_IDENTIFIER
import br.com.gazoza.alcoolougasolina.util.API_IDENTIFIER_OLD
import br.com.gazoza.alcoolougasolina.util.API_LANG
import br.com.gazoza.alcoolougasolina.util.API_PLATFORM
import br.com.gazoza.alcoolougasolina.util.API_PLATFORM_V
import br.com.gazoza.alcoolougasolina.util.API_V
import br.com.gazoza.alcoolougasolina.util.API_VERSION
import br.com.gazoza.alcoolougasolina.util.APP_HOST
import br.com.gazoza.alcoolougasolina.util.AppOpenManager
import br.com.gazoza.alcoolougasolina.util.PREF_DEVICE_ID
import br.com.gazoza.alcoolougasolina.util.PREF_DEVICE_ID_OLD
import br.com.gazoza.alcoolougasolina.util.isDebug
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.Locale

class CustomApplication : Application() {

    private var isCheckUpdatesNeeded: Boolean = true

    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        Hawk.init(this).build()

        MobileAds.initialize(this) {}

        AppOpenManager(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build()
        )

        FuelManager.instance.basePath = "${APP_HOST}api/${BuildConfig.API_APP_NAME}"

        updateFuelParams()
    }

    fun updateFuelParams() {
        FuelManager.instance.baseParams = listOf(
            API_IDENTIFIER to Hawk.get(PREF_DEVICE_ID, ""),
            API_IDENTIFIER_OLD to Hawk.get(PREF_DEVICE_ID_OLD, ""),
            API_LANG to Locale.getDefault().toString(),
            API_VERSION to BuildConfig.VERSION_CODE,
            API_PLATFORM to API_ANDROID,
            API_PLATFORM_V to Build.VERSION.SDK_INT,
            API_DEBUG to (if (isDebug()) "1" else "0"),
            API_V to 8
        )
    }

    fun setCheckUpdatesIsNeeded(isNeeded: Boolean) {
        isCheckUpdatesNeeded = isNeeded
    }

    fun getIsCheckUpdatesNeeded(): Boolean {
        return isCheckUpdatesNeeded
    }

}
