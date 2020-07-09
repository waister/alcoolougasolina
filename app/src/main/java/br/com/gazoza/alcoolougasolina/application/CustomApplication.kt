package br.com.gazoza.alcoolougasolina.application

import android.app.Application
import br.com.gazoza.alcoolougasolina.BuildConfig
import br.com.gazoza.alcoolougasolina.util.*
import com.github.kittinunf.fuel.core.FuelManager
import com.google.firebase.messaging.FirebaseMessaging
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import io.realm.RealmConfiguration

class CustomApplication : Application() {

    private var isCheckUpdatesNeeded: Boolean = true

    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        Hawk.init(this).build()

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build())

        FuelManager.instance.basePath = "${APP_HOST}api/${BuildConfig.API_APP_NAME}"

        updateFuelParams()
    }

    fun updateFuelParams() {
        FuelManager.instance.baseParams = listOf(
                API_IDENTIFIER to Hawk.get(PREF_DEVICE_ID, ""),
                API_VERSION to BuildConfig.VERSION_CODE,
                API_PLATFORM to API_ANDROID,
                API_DEBUG to (if (BuildConfig.DEBUG) "1" else "0"),
                API_V to 5
        )
    }

    fun setCheckUpdatesIsNeeded(isNeeded: Boolean) {
        isCheckUpdatesNeeded = isNeeded
    }

    fun getIsCheckUpdatesNeeded(): Boolean {
        return isCheckUpdatesNeeded
    }

}
