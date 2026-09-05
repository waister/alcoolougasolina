package br.com.gazoza.alcoolougasolina.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import br.com.gazoza.alcoolougasolina.util.LAST_ETHANOL
import br.com.gazoza.alcoolougasolina.util.LAST_GASOLINE
import br.com.gazoza.alcoolougasolina.util.PREF_APP_NAME
import br.com.gazoza.alcoolougasolina.util.PREF_DEVICE_ID
import br.com.gazoza.alcoolougasolina.util.PREF_DEVICE_ID_OLD
import br.com.gazoza.alcoolougasolina.util.PREF_FCM_TOKEN
import br.com.gazoza.alcoolougasolina.util.PREF_NOTIFICATION_JSON
import br.com.gazoza.alcoolougasolina.util.PREF_SHARE_LINK

interface PreferencesRepository {
    fun getLastEthanolPrice(): String
    fun setLastEthanolPrice(price: String)
    fun getLastGasolinePrice(): String
    fun setLastGasolinePrice(price: String)
    fun getDeviceId(): String
    fun setDeviceId(deviceId: String)
    fun getDeviceIdOld(): String
    fun setDeviceIdOld(deviceId: String)
    fun getFcmToken(): String
    fun setFcmToken(token: String)
    fun getShareLink(): String
    fun setShareLink(link: String)
    fun getAppName(): String
    fun setAppName(name: String)
    fun getNotificationJson(): String?
    fun setNotificationJson(json: String?)
    fun clear()
}

class PreferencesRepositoryImpl(
    private val context: Context
) : PreferencesRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_FILE_NAME = "app_prefs"
    }

    override fun getLastEthanolPrice(): String =
        sharedPreferences.getString(LAST_ETHANOL, "") ?: ""

    override fun setLastEthanolPrice(price: String) {
        sharedPreferences.edit { putString(LAST_ETHANOL, price) }
    }

    override fun getLastGasolinePrice(): String =
        sharedPreferences.getString(LAST_GASOLINE, "") ?: ""

    override fun setLastGasolinePrice(price: String) {
        sharedPreferences.edit { putString(LAST_GASOLINE, price) }
    }

    override fun getDeviceId(): String =
        sharedPreferences.getString(PREF_DEVICE_ID, "") ?: ""

    override fun setDeviceId(deviceId: String) {
        sharedPreferences.edit { putString(PREF_DEVICE_ID, deviceId) }
    }

    override fun getDeviceIdOld(): String =
        sharedPreferences.getString(PREF_DEVICE_ID_OLD, "") ?: ""

    override fun setDeviceIdOld(deviceId: String) {
        sharedPreferences.edit { putString(PREF_DEVICE_ID_OLD, deviceId) }
    }

    override fun getFcmToken(): String =
        sharedPreferences.getString(PREF_FCM_TOKEN, "") ?: ""

    override fun setFcmToken(token: String) {
        sharedPreferences.edit { putString(PREF_FCM_TOKEN, token) }
    }

    override fun getShareLink(): String =
        sharedPreferences.getString(PREF_SHARE_LINK, "") ?: ""

    override fun setShareLink(link: String) {
        sharedPreferences.edit { putString(PREF_SHARE_LINK, link) }
    }

    override fun getAppName(): String =
        sharedPreferences.getString(PREF_APP_NAME, "") ?: ""

    override fun setAppName(name: String) {
        sharedPreferences.edit { putString(PREF_APP_NAME, name) }
    }

    override fun getNotificationJson(): String? =
        sharedPreferences.getString(PREF_NOTIFICATION_JSON, null)

    override fun setNotificationJson(json: String?) {
        sharedPreferences.edit { putString(PREF_NOTIFICATION_JSON, json) }
    }

    override fun clear() {
        sharedPreferences.edit { clear() }
    }
}
