package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import br.com.gazoza.alcoolougasolina.BuildConfig
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.databinding.ActivityMainBinding
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.util.*
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.messaging.FirebaseMessaging
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import io.realm.Sort
import org.jetbrains.anko.*
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), TextWatcher, View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private val realm = Realm.getDefaultInstance()
    private var interstitialAd: InterstitialAd? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdMob()
        initViews()

        binding.etEthanol.addTextChangedListener(this)
        binding.etGasoline.addTextChangedListener(this)

        binding.btCalculate.setOnClickListener(this)
        binding.btClear.setOnClickListener(this)
    }

    private fun initAdMob() = with(binding) {
        MobileAds.initialize(applicationContext) {
            appLog("MainActivity", "Mobile ads initialized")

            val deviceId = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(deviceId).build()
            MobileAds.setRequestConfiguration(configuration)

            loadAdBanner(llBanner, "ca-app-pub-6521704558504566/7944661753")

            createInterstitialAd()
        }
    }

    private fun initViews() = with(binding) {
        verifyButtonsState(showMessage = false, requestFocus = true)

        val lastEthanol = Hawk.get(LAST_ETHANOL, "")
        val lastGasoline = Hawk.get(LAST_GASOLINE, "")

        etEthanol.setText(lastEthanol)
        etGasoline.setText(lastGasoline)

        etEthanol.addTextChangedListener(MaskMoney(etEthanol))
        etGasoline.addTextChangedListener(MaskMoney(etGasoline))

        if (lastEthanol.isNotEmpty() && lastGasoline.isNotEmpty()) {
            submitAction(false)

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        } else {
            if (lastEthanol.isEmpty())
                showKeyboard(etEthanol)
            else if (lastGasoline.isEmpty())
                showKeyboard(etGasoline)
        }

        etGasoline.setOnEditorActionListener { _, _, _ ->
            submitAction()
            false
        }

        tvVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        checkTokenFcm()
    }

    private fun createInterstitialAd() {
        val id = "ca-app-pub-6521704558504566/4051651496"
        val request = AdRequest.Builder().build()

        InterstitialAd.load(this, id, request, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }

            override fun onAdLoaded(loadedAd: InterstitialAd) {
                interstitialAd = loadedAd
            }
        })
    }

    override fun onClick(view: View?) = with(binding) {
        when (view?.id) {
            R.id.bt_calculate -> {
                submitAction()
            }
            R.id.bt_clear -> {
                etEthanol.setText("")
                etGasoline.setText("")

                Hawk.delete(LAST_ETHANOL)
                Hawk.delete(LAST_GASOLINE)

                verifyButtonsState(showMessage = false, requestFocus = true)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        verifyButtonsState(showMessage = false, requestFocus = false)
    }

    private fun submitAction(showMessage: Boolean = true) {
        if (showMessage)
            interstitialAd?.show(this)

        var message = 0
        val priceEthanol = binding.etEthanol.getPrice()
        val priceGasoline = binding.etGasoline.getPrice()
        val textEthanol = binding.etEthanol.text.toString()
        val textGasoline = binding.etGasoline.text.toString()

        when {
            priceEthanol == 0.0 -> {
                binding.etEthanol.requestFocus()
                message = R.string.msg_require_ethanol
            }
            priceGasoline == 0.0 -> {
                binding.etGasoline.requestFocus()
                message = R.string.msg_require_gasoline
            }
            else -> {
                Hawk.put(LAST_ETHANOL, textEthanol)
                Hawk.put(LAST_GASOLINE, textGasoline)

                val proportion = priceEthanol / priceGasoline

                val icon: Int
                val text: Int

                if (proportion < 0.7) {
                    icon = R.drawable.ic_ethanol_36dp
                    text = R.string.msg_use_ethanol
                } else {
                    icon = R.drawable.ic_gasoline_36dp
                    text = R.string.msg_use_gasoline
                }

                binding.tvMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                binding.tvMessage.setText(text)

                val percentage = DecimalFormat("#.##").format(proportion * 100) + "%"
                binding.tvProportion.text = getString(R.string.msg_result, percentage)

                realm.executeTransaction {
                    var last = realm.where(Comparison::class.java)
                        .sort("timestamp", Sort.DESCENDING)
                        .findFirst()

                    if (last == null || last.priceEthanol != textEthanol || last.priceGasoline != textGasoline) {
                        last = Comparison()

                        val max = realm.where(Comparison::class.java).max("id")
                        last.id = if (max != null) max.toLong() + 1 else 1
                    }

                    last.priceEthanol = textEthanol
                    last.priceGasoline = textGasoline
                    last.proportion = proportion
                    last.percentage = percentage
                    last.timestamp = System.currentTimeMillis()

                    realm.copyToRealmOrUpdate(last)
                }

                verifyButtonsState(showMessage = true, requestFocus = false)

                hideKeyboard()
            }
        }

        if (showMessage && message > 0)
            toast(message)
    }

    private fun verifyButtonsState(showMessage: Boolean, requestFocus: Boolean) = with(binding) {
        val isEthanolEmpty = etEthanol.getPrice() == 0.0
        val isGasolineEmpty = etGasoline.getPrice() == 0.0

        if (requestFocus) {
            if (isEthanolEmpty)
                etEthanol.requestFocus()

            if (isGasolineEmpty)
                etGasoline.requestFocus()
        }

        btCalculate.changeButtonState(!isEthanolEmpty && !isGasolineEmpty)
        btClear.changeButtonState(!isEthanolEmpty || !isGasolineEmpty)

        if (showMessage) {
            tvMessage.show()
            tvProportion.show()
        } else {
            tvMessage.hide()
            tvProportion.hide()
        }
    }

    private fun Button.changeButtonState(enable: Boolean) {
        this.isEnabled = enable
        this.alpha = if (enable) 1f else 0.3f
    }

    private fun checkVersion() = with(binding) {
        val token = Hawk.get(PREF_FCM_TOKEN, "")

        if (token.isNotEmpty()) {
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result

                if (error == null && !isFinishing) {
                    val apiObj = data.getValidJSONObject()

                    if (apiObj.getBooleanVal(API_SUCCESS)) {
                        Hawk.put(PREF_SHARE_LINK, apiObj.getStringVal(API_SHARE_LINK))
                        Hawk.put(PREF_APP_NAME, apiObj.getStringVal(API_APP_NAME))

                        val versionLast = apiObj.getIntVal(API_VERSION_LAST)
                        val versionMin = apiObj.getIntVal(API_VERSION_MIN)

                        if (BuildConfig.VERSION_CODE < versionMin) {
                            alert(
                                getString(R.string.update_needed),
                                getString(R.string.update_title)
                            ) {
                                positiveButton(R.string.update_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.update_logout) { finish() }
                                onCancelled { finish() }
                            }.show()
                        } else if (BuildConfig.VERSION_CODE < versionLast) {
                            alert(
                                getString(R.string.update_available),
                                getString(R.string.update_title)
                            ) {
                                positiveButton(R.string.update_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.update_negative) {}
                            }.show()
                        }
                    }
                }
            }

        }
    }

    private fun checkTokenFcm() {
        val currentToken = Hawk.get(PREF_FCM_TOKEN, "")

        appLog(TAG, "Current token: $currentToken")

        if (currentToken.isNotEmpty()) {
            checkVersion()
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isComplete) {
                    val token = it.result?.toString()

                    appLog(TAG, "New token: $token")

                    if (token != null) {
                        Hawk.put(PREF_FCM_TOKEN, token)

                        checkVersion()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_history -> {
                startActivity(intentFor<HistoryActivity>())
            }
            R.id.action_share -> {
                val app = Hawk.get(PREF_APP_NAME, "")
                val link = Hawk.get(PREF_SHARE_LINK, storeAppLink())

                share(getString(R.string.share_text, link), getString(R.string.share_subject, app))
            }
            R.id.action_rate -> {
                browse(storeAppLink())
            }
            R.id.action_notifications -> {
                startActivity(intentFor<NotificationsActivity>())
            }
            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}
