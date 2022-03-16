package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.util.*
import com.github.kittinunf.fuel.httpGet
import com.orhanobut.hawk.Hawk
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_notification_details.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.json.JSONObject

class NotificationDetailsActivity : AppCompatActivity() {

    private var notificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        notificationId = intent.getStringExtra(PARAM_ITEM_ID) ?: ""

        val notificationObj = Hawk.get<JSONObject>(PREF_NOTIFICATION_JSON + notificationId)

        if (notificationObj != null)
            renderNotification(notificationObj)
        else
            loadNotification()
    }

    private fun loadNotification() {
        pb_loading.visibility = View.VISIBLE

        val routeApi = API_ROUTE_NOTIFICATION + notificationId

        routeApi.httpGet().responseString { request, _, result ->
            appLog("NotificationDetails", "Request: $request")

            if (pb_loading == null) return@responseString

            pb_loading.visibility = View.GONE

            var errorMessage = getString(R.string.error_connection)

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
                alert(errorMessage, getString(R.string.ops)) {
                    okButton { finish() }
                    onCancelled { finish() }
                }.show()
            }
        }
    }

    private fun renderNotification(notificationObj: JSONObject) {
        val title = notificationObj.getStringVal(API_TITLE)
        var message = notificationObj.getStringVal(API_BODY)
        val date = notificationObj.getStringVal(API_DATE)
        val image = notificationObj.getStringVal(API_IMAGE)
        val link = notificationObj.getStringVal(API_LINK)

        if (link.isNotEmpty())
            message += "\n\n" + getString(R.string.label_link, link)

        tv_title.text = title
        tv_message.text = message
        tv_date.text = getString(R.string.label_received, date.formatDatetime())

        if (image.isValidUrl()) {
            Picasso.get()
                .load(getThumbUrl(image))
                .placeholder(R.drawable.ic_image_loading)
                .error(R.drawable.ic_image_error)
                .into(iv_image)
        } else {
            iv_image.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
