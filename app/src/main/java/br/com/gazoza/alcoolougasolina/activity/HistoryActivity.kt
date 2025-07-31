package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.adapter.HistoryAdapter
import br.com.gazoza.alcoolougasolina.application.CustomApplication
import br.com.gazoza.alcoolougasolina.databinding.ActivityHistoryBinding
import br.com.gazoza.alcoolougasolina.util.alert
import br.com.gazoza.alcoolougasolina.util.appLog
import br.com.gazoza.alcoolougasolina.util.hide
import br.com.gazoza.alcoolougasolina.util.loadAdBanner
import br.com.gazoza.alcoolougasolina.util.negativeButton
import br.com.gazoza.alcoolougasolina.util.positiveButton
import br.com.gazoza.alcoolougasolina.util.setupCommonInsets
import br.com.gazoza.alcoolougasolina.util.show
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private var historyAdapter: HistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.incToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initAdMob()
        initViews()
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
                positiveButton(R.string.clear_history) {
                    val dao = CustomApplication.database.comparisonDao()
                    dao.deleteAll()

                    initViews()
                }
                negativeButton(R.string.cancel) {}
            }.show()
        } else {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews() = with(binding) {
        setupCommonInsets(incToolbar.appBarLayout, root)

        val dao = CustomApplication.database.comparisonDao()
        val history = dao.getAllComparisons()

        if (history.isEmpty()) {
            tvHistoryEmpty.show()
            rvHistory.hide()
            return@with
        }

        tvHistoryEmpty.hide()
        rvHistory.show()

        rvHistory.setHasFixedSize(true)

        val columns = if (resources.displayMetrics.widthPixels > 1900) 2 else 1

        val layoutManager = GridLayoutManager(applicationContext, columns)
        rvHistory.layoutManager = layoutManager

        historyAdapter = HistoryAdapter(applicationContext)

        rvHistory.adapter = historyAdapter

        val divider = DividerItemDecoration(applicationContext, layoutManager.orientation)
        rvHistory.addItemDecoration(divider)

        historyAdapter?.setData(history)
    }

}
