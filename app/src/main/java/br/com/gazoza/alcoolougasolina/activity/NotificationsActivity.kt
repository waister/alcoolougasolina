package br.com.gazoza.alcoolougasolina.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.adapter.NotificationsAdapter
import br.com.gazoza.alcoolougasolina.util.*
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.inc_progress_light.*
import org.json.JSONArray

class NotificationsActivity : AppCompatActivity() {

    private var notifications: JSONArray? = null
    private var notificationsAdapter: NotificationsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadNotifications()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_RELOAD && resultCode == Activity.RESULT_OK) {
            loadNotifications()
        }
    }

    private fun loadNotifications() {
        rl_progress_light.visibility = View.VISIBLE
        tv_notifications_empty.visibility = View.GONE

        API_ROUTE_NOTIFICATIONS.httpGet().responseString { request, response, result ->
            printFuelLog(request, response, result)

            if (ly_main == null) return@responseString

            var errorMessage = getString(R.string.error_connection)

            rl_progress_light.visibility = View.GONE

            val (data, error) = result

            if (error == null) {
                val apiObj = data.getValidJSONObject()

                errorMessage = apiObj.getStringVal(API_MESSAGE)

                if (apiObj.getBooleanVal(API_SUCCESS)) {
                    errorMessage = ""

                    notifications = apiObj.getJSONArrayVal(API_NOTIFICATIONS)
                }
            }

            if (errorMessage.isNotEmpty()) {
                tv_notifications_empty.visibility = View.VISIBLE
                tv_notifications_empty.text = errorMessage
            } else {
                renderNotifications()
            }
        }
    }

    private fun renderNotifications() {
        if (notifications == null || notifications!!.length() == 0) {
            tv_notifications_empty.setText(R.string.notifications_empty)
            tv_notifications_empty.visibility = View.VISIBLE
            rv_notifications.visibility = View.GONE
            return
        }

        tv_notifications_empty.visibility = View.GONE
        rv_notifications.visibility = View.VISIBLE

        rv_notifications.setHasFixedSize(true)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val columns = if (displayMetrics.widthPixels > 1900) 2 else 1

        val layoutManager = GridLayoutManager(this, columns)
        rv_notifications.layoutManager = layoutManager

        notificationsAdapter = NotificationsAdapter(this)

        rv_notifications.adapter = notificationsAdapter

        val divider = DividerItemDecoration(this, layoutManager.orientation)
        rv_notifications.addItemDecoration(divider)

        notificationsAdapter?.setData(notifications)
    }

}
