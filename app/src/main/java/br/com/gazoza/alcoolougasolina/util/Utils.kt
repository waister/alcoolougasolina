package br.com.gazoza.alcoolougasolina.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.Log
import android.webkit.URLUtil
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.graphics.createBitmap
import br.com.gazoza.alcoolougasolina.BuildConfig
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

fun AppCompatEditText.getPrice(): Double {
    val value = this.text.toString()

    if (value.isNotEmpty())
        return value.replace(Regex("[^0-9]"), "").toDouble() / 100

    return 0.0
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
        val number = this.replace("\\D".toRegex(), "")
        if (number.isNotEmpty())
            return number.toInt()
    }
    return 0
}

fun String?.isValidUrl(): Boolean = !this.isNullOrEmpty() && URLUtil.isValidUrl(this)

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
            output = createBitmap(bitmap.width, bitmap.height)
            val canvas = Canvas(output)

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
            e.printOrReport()
        }
    }

    return output
}

fun String?.getStringValid(): String {
    if (!this.isNullOrEmpty() && this != "null" && this != "[null]") {
        return this
    }
    return ""
}

fun String?.formatDatetime(): String {
    try {
        if (!this.isNullOrEmpty()) {
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

fun isDebug() = BuildConfig.DEBUG

fun Throwable.printOrReport() {
    if (isDebug()) {
        this.printStackTrace()
    } else {
        FirebaseCrashlytics.getInstance().recordException(this)
    }
}
