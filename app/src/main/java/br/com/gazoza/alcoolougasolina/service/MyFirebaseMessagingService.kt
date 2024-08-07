package br.com.gazoza.alcoolougasolina.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import br.com.gazoza.alcoolougasolina.BuildConfig
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.activity.StartActivity
import br.com.gazoza.alcoolougasolina.util.API_ABOUT_APP
import br.com.gazoza.alcoolougasolina.util.API_FEEDBACK
import br.com.gazoza.alcoolougasolina.util.API_NOTIFICATIONS
import br.com.gazoza.alcoolougasolina.util.API_ROUTE_IDENTIFY
import br.com.gazoza.alcoolougasolina.util.API_TOKEN
import br.com.gazoza.alcoolougasolina.util.API_WAKEUP
import br.com.gazoza.alcoolougasolina.util.PARAM_ID
import br.com.gazoza.alcoolougasolina.util.PARAM_ITEM_ID
import br.com.gazoza.alcoolougasolina.util.PARAM_TYPE
import br.com.gazoza.alcoolougasolina.util.PREF_FCM_TOKEN
import br.com.gazoza.alcoolougasolina.util.appLog
import br.com.gazoza.alcoolougasolina.util.getCircleCroppedBitmap
import br.com.gazoza.alcoolougasolina.util.getThumbUrl
import br.com.gazoza.alcoolougasolina.util.isDebug
import br.com.gazoza.alcoolougasolina.util.isValidUrl
import br.com.gazoza.alcoolougasolina.util.printFuelLog
import br.com.gazoza.alcoolougasolina.util.sendNotificationReport
import br.com.gazoza.alcoolougasolina.util.storeAppLink
import br.com.gazoza.alcoolougasolina.util.stringToInt
import com.github.kittinunf.fuel.httpGet
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orhanobut.hawk.Hawk
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val ID = "id"
        const val TYPE = "type"
        const val TITLE = "title"
        const val BODY = "body"
        const val LINK = "link"
        const val IMAGE = "image"
        const val VERSION = "version"
        const val ITEM_ID = "item_id"
        const val VIBRATE = "vibrate"

        const val TAG = "MyFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        appLog(TAG, "New token: $token")

        val latToken = Hawk.get(PREF_FCM_TOKEN, "")

        if (token != latToken) {
            Hawk.put(PREF_FCM_TOKEN, token)

            val params = listOf(API_TOKEN to token)
            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        appLog(TAG, "Firebase Cloud Messaging new message received!")

        val data = remoteMessage.data

        appLog(TAG, "Push message data: $data")

        var id = ""
        var type = ""
        var title = ""
        var body = ""
        var link = ""
        var image = ""
        var version = ""
        var itemId = ""
        var vibrate = ""

        for (entry in data.entries) {
            val value = entry.value

            when (entry.key) {
                ID -> id = value
                TYPE -> type = value
                TITLE -> title = value
                BODY -> body = value
                LINK -> link = value
                IMAGE -> image = value
                VERSION -> version = value
                ITEM_ID -> itemId = value
                VIBRATE -> vibrate = value
            }
        }

        appLog(TAG, "Push id: $id")
        appLog(TAG, "Push type: $type")
        appLog(TAG, "Push title: $title")
        appLog(TAG, "Push body: $body")
        appLog(TAG, "Push link: $link")
        appLog(TAG, "Push image: $image")
        appLog(TAG, "Push version: $version")
        appLog(TAG, "Push itemId: $itemId")
        appLog(TAG, "Push vibrate: $vibrate")

        sendNotificationReport(id, true)

        if (title.isEmpty() || type == API_WAKEUP)
            return

        val channelId = "${type}_channel"

        var notifyIntent = Intent(applicationContext, StartActivity::class.java)

        if (version.isNotEmpty()) {
            val versionCode = version.stringToInt()

            if (versionCode > 0) {
                if (BuildConfig.VERSION_CODE < versionCode) {
                    link = applicationContext.storeAppLink()
                } else {
                    return
                }
            }
        }

        if (link.isValidUrl()) {

            notifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))

        } else {

            notifyIntent.putExtra(PARAM_ID, id)
            notifyIntent.putExtra(PARAM_TYPE, type)
            notifyIntent.putExtra(PARAM_ITEM_ID, itemId)

        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(notifyIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        builder.setAutoCancel(true)
        builder.setContentIntent(pendingIntent)
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.color = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        if (body.length > 40) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        if (image.isNotEmpty()) {
            val thumbUrl = getThumbUrl(image, 100, 100)

            appLog(TAG, "Push thumb url: $thumbUrl")

            try {
                val url = URL(thumbUrl)
                val icon = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                if (icon != null) {
                    builder.setLargeIcon(icon.getCircleCroppedBitmap())
                }
            } catch (e: Exception) {
                if (isDebug()) e.printStackTrace()
            }
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (type) {
                API_FEEDBACK -> R.string.feedback
                API_NOTIFICATIONS -> R.string.notifications
                API_ABOUT_APP -> R.string.about_app
                else -> R.string.channel_updates
            }
            val channel =
                NotificationChannel(channelId, getString(name), NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        manager.notify(1, builder.build())

        appLog(TAG, "Push notification displayed")

        appLog(TAG, "Push notification displayed - vibrate: $vibrate")

        if (vibrate.isNotEmpty()) {
            val pattern = longArrayOf(0, 100, 0, 100)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        pattern,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

}
