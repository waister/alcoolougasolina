package br.com.gazoza.alcoolougasolina.util

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import br.com.gazoza.alcoolougasolina.BuildConfig
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.orhanobut.hawk.Hawk
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun AppCompatEditText.getPrice(): Double {
    val value = this.text.toString()

    if (value.isNotEmpty())
        return value.replace(Regex("[^0-9]"), "").toDouble() / 100

    return 0.0
}

fun Activity?.showKeyboard(view: View) {
    if (this != null) {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        imm.showSoftInput(view, 0)
    }
}

fun Activity?.hideKeyboard() {
    if (this != null) {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = currentFocus
        if (view == null)
            view = View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}


fun printFuelLog(request: Request, response: Response, result: Result<String, FuelError>) {
    Log.w("FUEL_API_CALL", "API was called to route: ${request.path}")

    if (BuildConfig.DEBUG) {
        val path = request.path

        println("\n------------ FUEL_REQUEST_START - $path\n")
        println(request)
        println("\n------------ FUEL_REQUEST_END - $path\n")

        println("\n------------ FUEL_RESPONSE_START - $path\n")
        println(response)
        println("\n------------ FUEL_RESPONSE_END - $path\n")

        println("\n------------ FUEL_RESULT_START - $path\n")
        println(result)
        println("\n------------ FUEL_RESULT_END - $path\n")
    }
}

fun saveAppData(result: Result<String, FuelError>) {
    val (data, error) = result

    if (error == null) {
        val apiObj = data.getValidJSONObject()

        if (apiObj.getBooleanVal(API_SUCCESS)) {
            Hawk.put(PREF_SHARE_LINK, apiObj.getStringVal(API_SHARE_LINK))
            Hawk.put(PREF_APP_NAME, apiObj.getStringVal(API_APP_NAME))
            Hawk.put(PREF_NAME_TO_REMOVE, apiObj.getStringVal(API_NAME_TO_REMOVE))
            Hawk.put(PREF_ADMOB_ID, apiObj.getStringVal(API_ADMOB_ID))
            Hawk.put(PREF_ADMOB_AD_MAIN_ID, apiObj.getStringVal(API_ADMOB_AD_MAIN_ID))
            Hawk.put(PREF_ADMOB_INTERSTITIAL_ID, apiObj.getStringVal(API_ADMOB_INTERSTITIAL_ID))
            Hawk.put(PREF_ADMOB_REMOVE_ADS_ID, apiObj.getStringVal(API_ADMOB_REMOVE_ADS_ID))
            Hawk.put(PREF_BILL_PLAN_YEAR, apiObj.getStringVal(API_BILL_PLAN_YEAR))
            Hawk.put(PREF_PLAN_VIDEO_DURATION, apiObj.getLongVal(API_PLAN_VIDEO_DURATION))
        }
    }
}

fun String?.stringToInt(): Int {
    if (this != null && this != "null") {
        val number = this.replace("[^\\d]".toRegex(), "")
        if (number.isNotEmpty())
            return number.toInt()
    }
    return 0
}

fun String?.isValidUrl(): Boolean = this != null && this.isNotEmpty() && URLUtil.isValidUrl(this)

fun storeAppLink(): String =
    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"


fun Context?.getThumbUrl(
    image: String?,
    width: Int = 220,
    height: Int = 0,
    quality: Int = 85
): String {
    if (this != null && image != null && !image.contains("http") && image.contains("/uploads/")) {
        return APP_HOST + "thumb?src=$image&w=$width&h=$height&q=$quality"
    }

    return image.getApiImage()
}

fun String?.getApiImage(): String {
    if (this != null) {
        if (!contains("http") && contains("/uploads/")) {
            val path = APP_HOST.removeSuffix("/") + this

            if (path.isValidUrl()) {
                return path
            }
        }

        return this
    }

    return ""
}

fun Bitmap?.getCircleCroppedBitmap(): Bitmap? {
    var output: Bitmap? = null
    val bitmap = this

    if (bitmap != null) {
        try {
            output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output!!)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            if (bitmap.width < bitmap.height) {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.width / 2).toFloat(), paint
                )
            } else {
                canvas.drawCircle(
                    (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.height / 2).toFloat(), paint
                )
            }
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return output
}

fun String?.getStringValid(): String {
    if (this != null && this.isNotEmpty() && this != "null" && this != "[null]") {
        return this
    }
    return ""
}

fun String?.formatDatetime(): String {
    try {
        if (this != null && this.isNotEmpty()) {
            val locale = Locale.getDefault()
            val parsed = SimpleDateFormat(FORMAT_DATETIME_API, locale).parse(this)

            if (parsed != null)
                return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                    .format(parsed.time)
        }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

fun Long.formatDatetime(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(this)
}

fun haveVideoPlan(): Boolean {
    val planVideoMillis = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L)
    if (planVideoMillis != 0L) {
        val panVideoDuration = Hawk.get(PREF_PLAN_VIDEO_DURATION, FIVE_DAYS)
        val expiration = Hawk.get(PREF_PLAN_VIDEO_MILLIS, 0L) + panVideoDuration
        return expiration > System.currentTimeMillis()
    }
    return false
}

fun haveBillingPlan(): Boolean = Hawk.get(PREF_HAVE_PLAN, !BuildConfig.DEBUG)

fun havePlan(): Boolean = haveBillingPlan() || haveVideoPlan()

fun Activity?.loadAdBanner(root: LinearLayout?, adUnitId: String, adSize: AdSize?) {
    if (this == null || root == null || havePlan()) return

    val testAdUnitId = "ca-app-pub-3940256099942544/6300978111"

    val adView = AdView(this)
    adView.adSize = adSize ?: AdSize.SMART_BANNER
    adView.adUnitId = if (BuildConfig.DEBUG) testAdUnitId else adUnitId

    root.addView(
        adView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    )

    adView.loadAd(AdRequest.Builder().build())
}