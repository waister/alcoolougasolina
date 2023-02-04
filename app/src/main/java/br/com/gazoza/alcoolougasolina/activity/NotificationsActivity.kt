package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.adapter.NotificationsAdapter
import br.com.gazoza.alcoolougasolina.databinding.ActivityNotificationsBinding
import br.com.gazoza.alcoolougasolina.util.*
import com.github.kittinunf.fuel.httpGet
import org.json.JSONArray

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    private var notifications: JSONArray? = null
    private var notificationsAdapter: NotificationsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadNotifications()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun loadNotifications() = with(binding) {
        llLoading.show()
        tvNotificationsEmpty.hide()

        API_ROUTE_NOTIFICATIONS.httpGet().responseString { request, response, result ->
            printFuelLog(request, response, result)

            var errorMessage = getString(R.string.error_connection)

            llLoading.hide()

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
                tvNotificationsEmpty.show()
                tvNotificationsEmpty.text = errorMessage
            } else {
                renderNotifications()
            }
        }
    }

    private fun renderNotifications() = with(binding) {
        if (notifications == null || notifications!!.length() == 0) {
            tvNotificationsEmpty.setText(R.string.notifications_empty)
            tvNotificationsEmpty.show()
            rvNotifications.hide()
            return@with
        }

        tvNotificationsEmpty.hide()
        rvNotifications.show()

        rvNotifications.setHasFixedSize(true)

        val columns = if (displayWidth() > 1900) 2 else 1

        val layoutManager = GridLayoutManager(applicationContext, columns)
        rvNotifications.layoutManager = layoutManager

        notificationsAdapter = NotificationsAdapter(applicationContext)

        rvNotifications.adapter = notificationsAdapter

        val divider = DividerItemDecoration(rvNotifications.context, layoutManager.orientation)
        divider.setDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.recycler_divider
            )!!
        )
        rvNotifications.addItemDecoration(divider)

        notificationsAdapter?.setData(notifications)
    }

}
