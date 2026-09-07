package br.com.gazoza.alcoolougasolina.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONObject

@Suppress("unused")
object StorageHelper {
    private const val PREF_FILE_NAME = "app_prefs"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun put(key: String, value: String?) {
        sharedPreferences.edit { putString(key, value) }
    }

    fun put(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    fun put(key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }

    fun put(key: String, value: JSONObject) {
        sharedPreferences.edit { putString(key, value.toString()) }
    }

    fun get(key: String, defaultValue: String): String = sharedPreferences.getString(key, defaultValue) ?: defaultValue

    fun get(key: String, defaultValue: Boolean): Boolean = sharedPreferences.getBoolean(key, defaultValue)

    fun get(key: String, defaultValue: Int): Int = sharedPreferences.getInt(key, defaultValue)

    fun getJSONObject(key: String): JSONObject? {
        val jsonString = sharedPreferences.getString(key, null)
        return if (jsonString != null) {
            try {
                JSONObject(jsonString)
            } catch (e: Exception) {
                e.printOrReport()
                null
            }
        } else {
            null
        }
    }

    fun delete(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }
}
