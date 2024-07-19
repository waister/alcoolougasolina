package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.databinding.ActivityNotificationDetailsBinding
import br.com.gazoza.alcoolougasolina.util.API_BODY
import br.com.gazoza.alcoolougasolina.util.API_DATE
import br.com.gazoza.alcoolougasolina.util.API_IMAGE
import br.com.gazoza.alcoolougasolina.util.API_LINK
import br.com.gazoza.alcoolougasolina.util.API_MESSAGE
import br.com.gazoza.alcoolougasolina.util.API_NOTIFICATION
import br.com.gazoza.alcoolougasolina.util.API_ROUTE_NOTIFICATION
import br.com.gazoza.alcoolougasolina.util.API_TITLE
import br.com.gazoza.alcoolougasolina.util.PARAM_ITEM_ID
import br.com.gazoza.alcoolougasolina.util.PREF_NOTIFICATION_JSON
import br.com.gazoza.alcoolougasolina.util.appLog
import br.com.gazoza.alcoolougasolina.util.formatDatetime
import br.com.gazoza.alcoolougasolina.util.getJSONObjectVal
import br.com.gazoza.alcoolougasolina.util.getStringVal
import br.com.gazoza.alcoolougasolina.util.getThumbUrl
import br.com.gazoza.alcoolougasolina.util.getValidJSONObject
import br.com.gazoza.alcoolougasolina.util.hide
import br.com.gazoza.alcoolougasolina.util.isValidUrl
import br.com.gazoza.alcoolougasolina.util.show
import com.github.kittinunf.fuel.httpGet
import com.orhanobut.hawk.Hawk
import com.squareup.picasso.Picasso
import org.json.JSONObject

class NotificationDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDetailsBinding

    private val context = this@NotificationDetailsActivity
    private var notificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        notificationId = intent.getStringExtra(PARAM_ITEM_ID) ?: ""

        val notificationObj = Hawk.get<JSONObject>(PREF_NOTIFICATION_JSON + notificationId)

        if (notificationObj != null) {

            renderNotification(notificationObj)

        } else {

            loadNotification()

        }
    }

    private fun loadNotification() = with(binding) {
        llLoading.show()

        val routeApi = API_ROUTE_NOTIFICATION + notificationId

        routeApi.httpGet().responseString { request, _, result ->
            appLog("NotificationDetails", "Request: $request")

            var errorMessage = getString(R.string.error_connection)

            llLoading.hide()

            val (data, error) = result

            if (error == null) {
                val apiObj = data.getValidJSONObject()

                errorMessage = apiObj.getStringVal(API_MESSAGE)

                val notificationObj = apiObj.getJSONObjectVal(API_NOTIFICATION)

                if (notificationObj != null) {
                    errorMessage = ""

                    Hawk.put(PREF_NOTIFICATION_JSON + notificationId, notificationObj)

                    renderNotification(notificationObj)
                }
            }

            if (errorMessage.isNotEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.ops)
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok) { dialog, _ ->
                        finish()

                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun renderNotification(notificationObj: JSONObject) = with(binding) {
        val title = notificationObj.getStringVal(API_TITLE)
        var message = notificationObj.getStringVal(API_BODY)
        val date = notificationObj.getStringVal(API_DATE)
        val image = notificationObj.getStringVal(API_IMAGE)
        val link = notificationObj.getStringVal(API_LINK)

        if (link.isNotEmpty())
            message += "\n\n" + getString(R.string.label_link, link)

        tvTitle.text = title
        tvMessage.text = message
        tvDate.text = getString(R.string.label_received, date.formatDatetime())

        if (image.isValidUrl()) {
            Picasso.get()
                .load(getThumbUrl(image))
                .placeholder(R.drawable.ic_image_loading)
                .error(R.drawable.ic_image_error)
                .into(ivImage)
        } else {
            ivImage.hide()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
