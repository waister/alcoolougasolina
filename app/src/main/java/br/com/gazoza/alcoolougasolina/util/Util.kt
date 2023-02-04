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
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.android.gms.ads.*
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
    Log.w("FUEL_API_CALL", "API was called to route: ${request.url}")

    if (isDebug()) {
        val url = request.url

        println("\n------------ FUEL_REQUEST_START - $url\n")
        println(request)
        println("\n------------ FUEL_REQUEST_END - $url\n")

        println("\n------------ FUEL_RESPONSE_START - $url\n")
        println(response)
        println("\n------------ FUEL_RESPONSE_END - $url\n")

        println("\n------------ FUEL_RESULT_START - $url\n")
        println(result)
        println("\n------------ FUEL_RESULT_END - $url\n")
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

fun String?.formatDate(): String {
    try {
        if (this != null && this.isNotEmpty()) {
            val locale = Locale.getDefault()
            val parsed = SimpleDateFormat(FORMAT_DATETIME_API, locale).parse(this)

            if (parsed != null)
                DateFormat.getDateInstance(DateFormat.SHORT).format(parsed.time)
        }
    } catch (e: ParseException) {
        if (isDebug()) e.printStackTrace()
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

fun Activity?.loadAdBanner(
    adViewContainer: LinearLayout?,
    adUnitId: String,
    adSize: AdSize? = null
) {
    if (this == null || adViewContainer == null) {
        appLog("LOAD_BANNER", "Can't show ad")
        return
    }

    appLog("LOAD_BANNER", "adUnitId: $adUnitId")

    val adView = AdView(this)
    adViewContainer.addView(adView)

    adView.adUnitId = if (isDebug()) "ca-app-pub-3940256099942544/6300978111" else adUnitId

    adView.setAdSize(adSize ?: getAdSize(adViewContainer))

    adView.loadAd(AdRequest.Builder().build())

    adView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            appLog("LOAD_BANNER", "onAdLoaded()")
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
            super.onAdFailedToLoad(error)
            appLog("LOAD_BANNER", "onAdFailedToLoad(): ${error.message}")
        }

        override fun onAdOpened() {
            super.onAdOpened()
            appLog("LOAD_BANNER", "onAdOpened()")
        }

        override fun onAdClosed() {
            super.onAdClosed()
            appLog("LOAD_BANNER", "onAdClosed()")
        }
    }

    appLog("LOAD_BANNER", "ENDS")
}

fun Activity.getAdSize(adViewContainer: LinearLayout): AdSize {
    var adWidthPixels = adViewContainer.width.toFloat()
    if (adWidthPixels == 0f)
        adWidthPixels = displayWidth().toFloat()

    val density = resources.displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}

fun Activity?.displayWidth(): Int {
    return if (this != null) resources.displayMetrics.widthPixels else 0
}

fun Context.storeAppLink(): String = "https://play.google.com/store/apps/details?id=$packageName"

fun sendNotificationReport(notificationId: String, isForReceived: Boolean) {
    if (notificationId.isEmpty()) return

    val millis = System.currentTimeMillis().toString()

    val params = mutableListOf(API_NOTIFICATION_ID to notificationId)

    if (isForReceived)
        params.add(API_RECEIVED_AT to millis)
    else
        params.add(API_CLICKED_AT to millis)

    API_ROUTE_NOTIFICATION_REPORT.httpGet(params).responseString { request, response, result ->
        printFuelLog(request, response, result)
    }
}

fun appLog(tag: String, msg: String) {
    if (isDebug())
        Log.i("MAGGAPPS_LOG", "➡➡➡ $tag: $msg")
}

fun String?.isNumeric(): Boolean {
    if (this == null) return false
    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return this.matches(regex)
}

fun String?.isNotNumeric(): Boolean {
    return !this.isNumeric()
}

fun View?.isVisible(isVisible: Boolean) {
    this?.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View?.show() {
    this.isVisible(true)
}

fun View?.hide() {
    this.isVisible(false)
}

fun isDebug() = BuildConfig.DEBUG