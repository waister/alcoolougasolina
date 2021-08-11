package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.adapter.HistoryAdapter
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.util.appLog
import br.com.gazoza.alcoolougasolina.util.loadAdBanner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_history.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.displayMetrics

class HistoryActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()
    private var historyAdapter: HistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initAdMob()
        renderNotifications()
    }

    private fun initAdMob() {
        MobileAds.initialize(this) {
            appLog("HistoryActivity", "Mobile ads initialized")

            val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
            MobileAds.setRequestConfiguration(configuration)

            loadAdBanner(ll_banner, "ca-app-pub-6521704558504566/6221190272")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_clear) {
            alert(R.string.confirm_clear_history, R.string.confirmation) {
                also { ctx.setTheme(R.style.CustomAlertDialog) }
                positiveButton(R.string.clear_history) {
                    realm.executeTransaction {
                        realm.where(Comparison::class.java)
                            .findAll()
                            .deleteAllFromRealm()
                    }

                    renderNotifications()
                }
                negativeButton(R.string.cancel) {}
            }.show()
        } else {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun renderNotifications() {
        val history = realm.where(Comparison::class.java)
            .sort("timestamp", Sort.DESCENDING)
            .findAll()

        if (history == null || history.count() == 0) {
            tv_history_empty.visibility = View.VISIBLE
            rv_history.visibility = View.GONE
            return
        }

        tv_history_empty.visibility = View.GONE
        rv_history.visibility = View.VISIBLE

        rv_history.setHasFixedSize(true)

        val columns = if (displayMetrics.widthPixels > 1900) 2 else 1

        val layoutManager = GridLayoutManager(this, columns)
        rv_history.layoutManager = layoutManager

        historyAdapter = HistoryAdapter(this)

        rv_history.adapter = historyAdapter

        val divider = DividerItemDecoration(this, layoutManager.orientation)
        rv_history.addItemDecoration(divider)

        historyAdapter?.setData(history)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}
