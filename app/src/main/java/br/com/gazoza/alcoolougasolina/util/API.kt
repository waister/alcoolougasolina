package br.com.gazoza.alcoolougasolina.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val APP_HOST = "https://maggapps.com/"

const val API_ROUTE_IDENTIFY = "/identify"
const val API_ROUTE_SEND_MESSAGE = "/message/send"
const val API_ROUTE_MESSAGES = "/messages"
const val API_ROUTE_NOTIFICATIONS = "/notifications"
const val API_ROUTE_NOTIFICATION = "/notification/"

const val API_MAGAZINES = "magazines"
const val API_PAGES = "pages"
const val API_SUCCESS = "success"
const val API_VERSION_LAST = "version_last"
const val API_VERSION_MIN = "version_min"
const val API_SHARE_LINK = "store_link"
const val API_MESSAGE = "message"
const val API_USER_NAME = "user_name"
const val API_USER_EMAIL = "user_email"
const val API_ID = "id"
const val API_SLUG = "slug"
const val API_ANDROID = "android"
const val API_IDENTIFIER = "identifier"
const val API_VERSION = "version"
const val API_PLATFORM = "platform"
const val API_DEBUG = "debug"
const val API_CREATED = "created_at"
const val API_UPDATED = "updated_at"
const val API_DELETED = "deleted_at"
const val API_TITLE = "title"
const val API_LINK = "link"
const val API_BODY = "body"
const val API_DATE = "date"
const val API_IMAGE = "image"
const val API_IMAGE_RATIO = "image_ratio"
const val API_TOKEN = "token"
const val API_IS_CURRENT = "is_current"
const val API_NAME = "name"
const val API_INSTAGRAM = "instagram"
const val API_PATH = "path"
const val API_EMAIL = "email"
const val API_COMMENTS = "comments"
const val API_CATEGORY = "category"
const val API_AUTHOR = "author"
const val API_ADMIN = "admin"
const val API_V = "api_v"
const val API_PAGES_COUNT = "pages_count"
const val API_MAGAZINE_ID = "magazine_id"
const val API_FILE_LINK = "file_link"
const val API_FILE_SIZE = "file_size"
const val API_FILE_SIZE_SHOW = "file_size_show"
const val API_STATE_ID = "state_id"
const val API_CITY_ID = "city_id"
const val API_RESELLERS = "resellers"
const val API_MESSAGES = "messages"
const val API_NOTIFICATIONS = "notifications"
const val API_NOTIFICATION = "notification"
const val API_CITY = "city"
const val API_CITIES = "cities"
const val API_STATE = "state"
const val API_STATES = "states"
const val API_WHATSAPP = "whatsapp"
const val API_PHONE = "phone"
const val API_LIST = "list"
const val API_PREMIUM = "premium"
const val API_FEEDBACK = "feedback"
const val API_COMPARATOR = "comparator"
const val API_ABOUT_APP = "about_app"
const val API_WAKEUP = "wakeup"

const val API_APP_NAME = "app_name"
const val API_NAME_TO_REMOVE = "name_to_remove"
const val API_ADMOB_ID = "admob_id"
const val API_ADMOB_AD_MAIN_ID = "admob_ad_main_id"
const val API_ADMOB_INTERSTITIAL_ID = "admob_interstitial_id"
const val API_ADMOB_REMOVE_ADS_ID = "admob_remove_ads_id"
const val API_PLAN_VIDEO_DURATION = "plan_video_duration"
const val API_BILL_PLAN_YEAR = "bill_plan_year"

fun JSONObject?.getArrayValid(tag: String): JSONArray? {
    if (this != null) {
        try {
            return getJSONArray(tag)
        } catch (e: JSONException) {
        }
    }
    return null
}

fun JSONObject?.getObjectValid(tag: String): JSONObject? {
    if (this != null) {
        try {
            return getJSONObject(tag)
        } catch (e: JSONException) {
        }
    }
    return null
}

fun JSONObject?.getStringVal(tag: String, default: String = ""): String {
    if (this != null && has(tag)) {
        try {
            return getString(tag).getStringValid()
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getIntVal(tag: String, default: Int = 0): Int {
    if (this != null && has(tag)) {
        try {
            return getInt(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getLongVal(tag: String, default: Long = 0): Long {
    if (this != null && has(tag)) {
        try {
            return getLong(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getDoubleVal(tag: String, default: Double = 0.0): Double {
    if (this != null && has(tag)) {
        try {
            return getDouble(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}

fun JSONObject?.getBooleanVal(tag: String, default: Boolean = false): Boolean {
    if (this != null && has(tag)) {
        try {
            return getBoolean(tag)
        } catch (e: JSONException) {
        }
    }
    return default
}

fun String?.getValidJSONObject(): JSONObject? {
    if (this != null && this.isNotEmpty() && this != "null") {
        try {
            return JSONObject(this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun String?.getValidJSONArray(): JSONArray? {
    if (this != null && this.isNotEmpty() && this != "null") {
        try {
            return JSONArray(this)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun JSONObject?.getJSONObjectVal(tag: String): JSONObject? {
    if (this != null && this.has(tag) && !this.isNull(tag)) {
        try {
            return this.getJSONObject(tag)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}

fun JSONObject?.getJSONArrayVal(tag: String): JSONArray? {
    if (this != null && this.has(tag) && !this.isNull(tag)) {
        try {
            return this.getJSONArray(tag)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return null
}