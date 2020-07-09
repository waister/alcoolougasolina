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
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.util.*
import com.appodeal.ads.Appodeal
import com.appodeal.ads.utils.Log
import com.github.kittinunf.fuel.httpGet
import com.google.firebase.iid.FirebaseInstanceId
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), TextWatcher, View.OnClickListener {

    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Appodeal.initialize(this, APPODEAL_APP_KEY, Appodeal.INTERSTITIAL or Appodeal.BANNER, true)

        if (BuildConfig.DEBUG) {
            Appodeal.setTesting(true)
            Appodeal.setLogLevel(Log.LogLevel.debug)
        }

        initViews()
    }

    private fun initViews() {
        verifyButtonsState(showMessage = false, requestFocus = true)

        val lastEthanol = Hawk.get(LAST_ETHANOL, "")
        val lastGasoline = Hawk.get(LAST_GASOLINE, "")

        et_ethanol.setText(lastEthanol)
        et_gasoline.setText(lastGasoline)

        et_ethanol.addTextChangedListener(MaskMoney(et_ethanol))
        et_gasoline.addTextChangedListener(MaskMoney(et_gasoline))

        if (lastEthanol.isNotEmpty() && lastGasoline.isNotEmpty()) {
            submitAction(false)

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        } else {
            if (lastEthanol.isEmpty())
                showKeyboard(et_ethanol)
            else if (lastGasoline.isEmpty())
                showKeyboard(et_gasoline)
        }

        et_ethanol.addTextChangedListener(this)
        et_gasoline.addTextChangedListener(this)

        bt_calculate.setOnClickListener(this)
        bt_clear.setOnClickListener(this)

        et_gasoline.setOnEditorActionListener { _, _, _ ->
            submitAction()
            false
        }

        tv_version.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        checkVersion()
        checkTokenFcm()

        if (!havePlan()) {
            Appodeal.setBannerViewId(R.id.appodealBannerView)
            Appodeal.show(this, Appodeal.BANNER_VIEW)

            Appodeal.cache(this, Appodeal.INTERSTITIAL)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.bt_calculate -> {
                submitAction()
            }
            R.id.bt_clear -> {
                et_ethanol.setText("")
                et_gasoline.setText("")

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
        var message = 0
        val priceEthanol = et_ethanol.getPrice()
        val priceGasoline = et_gasoline.getPrice()
        val textEthanol = et_ethanol.text.toString()
        val textGasoline = et_gasoline.text.toString()

        when {
            priceEthanol == 0.0 -> {
                et_ethanol.requestFocus()
                message = R.string.msg_require_ethanol
            }
            priceGasoline == 0.0 -> {
                et_gasoline.requestFocus()
                message = R.string.msg_require_gasoline
            }
            else -> {
                Hawk.put(LAST_ETHANOL, textEthanol)
                Hawk.put(LAST_GASOLINE, textGasoline)

                val proportion = priceEthanol / priceGasoline

                val icon: Int
                val text: Int

                if (proportion < 0.7) {
                    icon = R.mipmap.ic_ethanol_36dp
                    text = R.string.msg_use_ethanol
                } else {
                    icon = R.mipmap.ic_gasoline_36dp
                    text = R.string.msg_use_gasoline
                }

                tv_message.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                tv_message.setText(text)

                val percentage = DecimalFormat("#.##").format(proportion * 100) + "%"
                tv_proportion.text = getString(R.string.msg_result, percentage)

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

    private fun verifyButtonsState(showMessage: Boolean, requestFocus: Boolean) {
        val isEthanolEmpty = et_ethanol.getPrice() == 0.0
        val isGasolineEmpty = et_gasoline.getPrice() == 0.0

        if (requestFocus) {
            if (isEthanolEmpty)
                et_ethanol.requestFocus()

            if (isGasolineEmpty)
                et_gasoline.requestFocus()
        }

        bt_calculate.changeButtonState(!isEthanolEmpty && !isGasolineEmpty)
        bt_clear.changeButtonState(!isEthanolEmpty || !isGasolineEmpty)

        if (showMessage) {
            tv_message.visibility = View.VISIBLE
            tv_proportion.visibility = View.VISIBLE
        } else {
            tv_message.visibility = View.GONE
            tv_proportion.visibility = View.GONE
        }
    }

    private fun Button.changeButtonState(enable: Boolean) {
        this.isEnabled = enable
        this.alpha = if (enable) 1f else 0.3f
    }

    private fun checkVersion() {
        val token = Hawk.get(PREF_FCM_TOKEN, "")

        if (token.isNotEmpty()) {
            val params = listOf(API_TOKEN to token)

            API_ROUTE_IDENTIFY.httpGet(params).responseString { request, response, result ->
                printFuelLog(request, response, result)

                val (data, error) = result

                if (error == null && cl_root != null) {
                    val apiObj = data.getValidJSONObject()

                    if (apiObj.getBooleanVal(API_SUCCESS)) {
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
        if (Hawk.get(PREF_FCM_TOKEN, "").isNotEmpty()) return

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result?.token

                if (token != null) {
                    Hawk.put(PREF_FCM_TOKEN, token)

                    checkVersion()
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
            else -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (!havePlan() && Appodeal.isLoaded(Appodeal.INTERSTITIAL))
            Appodeal.show(this, Appodeal.INTERSTITIAL)
    }

}
