package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.adapter.HistoryAdapter
import br.com.gazoza.alcoolougasolina.databinding.ActivityHistoryBinding
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.util.appLog
import br.com.gazoza.alcoolougasolina.util.hide
import br.com.gazoza.alcoolougasolina.util.loadAdBanner
import br.com.gazoza.alcoolougasolina.util.show
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import io.realm.Realm
import io.realm.Sort
import org.jetbrains.anko.alert
import org.jetbrains.anko.displayMetrics

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private val realm = Realm.getDefaultInstance()
    private var historyAdapter: HistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initAdMob()
        renderNotifications()
    }

    private fun initAdMob() = with(binding) {
        MobileAds.initialize(applicationContext) {
            appLog("HistoryActivity", "Mobile ads initialized")

            val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
            MobileAds.setRequestConfiguration(configuration)

            loadAdBanner(llBanner, "ca-app-pub-6521704558504566/6221190272")
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
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun renderNotifications() = with(binding) {
        val history = realm.where(Comparison::class.java)
            .sort("timestamp", Sort.DESCENDING)
            .findAll()

        if (history.isNullOrEmpty()) {
            tvHistoryEmpty.show()
            rvHistory.hide()
            return@with
        }

        tvHistoryEmpty.hide()
        rvHistory.show()

        rvHistory.setHasFixedSize(true)

        val columns = if (displayMetrics.widthPixels > 1900) 2 else 1

        val layoutManager = GridLayoutManager(applicationContext, columns)
        rvHistory.layoutManager = layoutManager

        historyAdapter = HistoryAdapter(applicationContext)

        rvHistory.adapter = historyAdapter

        val divider = DividerItemDecoration(applicationContext, layoutManager.orientation)
        rvHistory.addItemDecoration(divider)

        historyAdapter?.setData(history)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}
