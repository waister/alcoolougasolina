package br.com.gazoza.alcoolougasolina.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import br.com.gazoza.alcoolougasolina.BuildConfig
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.domain.Comparation
import br.com.gazoza.alcoolougasolina.util.*
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.ads.MobileAds
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

        MobileAds.initialize(this, Hawk.get(PREF_ADMOB_ID, ""))

        initViews()
    }

    private fun initViews() {
        verifyButtonsState(showMessage = false, requestFocus = true)

        val lastEthanol = Hawk.get(LAST_ETHANOL, "")
        val lastGasoline = Hawk.get(LAST_GASOLINE, "")

        et_ethanol.setText(lastEthanol)
        et_gasoline.setText(lastGasoline)

        et_ethanol.maskMoney()
        et_gasoline.maskMoney()

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
        fab_history.setOnClickListener(this)
        fab_share.setOnClickListener(this)

        et_gasoline.setOnEditorActionListener { _, _, _ ->
            submitAction()
            false
        }

        loadAdBanner(Hawk.get(PREF_ADMOB_AD_MAIN_ID, ""))

        checkVersion()
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
            R.id.fab_history -> {
                startActivity(intentFor<HistoryActivity>())
            }
            R.id.fab_share -> {
                val app = Hawk.get(PREF_APP_NAME, "")
                val link = Hawk.get(PREF_SHARE_LINK, storeAppLink())

                share(getString(R.string.share_text, link), getString(R.string.share_subject, app))
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
                    icon = R.drawable.ic_ethanol_36dp
                    text = R.string.msg_use_ethanol
                } else {
                    icon = R.drawable.ic_gasoline_36dp
                    text = R.string.msg_use_gasoline
                }

                tv_message.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                tv_message.setText(text)

                val percentage = DecimalFormat("#.##").format(proportion * 100) + "%"
                tv_proportion.text = getString(R.string.msg_result, percentage)

                realm.executeTransaction {
                    var last = realm.where(Comparation::class.java)
                            .sort("millis", Sort.DESCENDING)
                            .findFirst()

                    if (last == null || last.priceEthanol != textEthanol || last.priceGasoline != textGasoline) {
                        last = Comparation()

                        val max = realm.where(Comparation::class.java).max("id")
                        last.id = if (max != null) max.toLong() + 1 else 1
                    }

                    last.priceEthanol = textEthanol
                    last.priceGasoline = textGasoline
                    last.proportion = proportion
                    last.percentage = percentage
                    last.millis = System.currentTimeMillis()

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
                            alert(getString(R.string.update_needed), getString(R.string.update_title)) {
                                positiveButton(R.string.update_positive) {
                                    browse(storeAppLink())
                                }
                                negativeButton(R.string.update_logout) { finish() }
                                onCancelled { finish() }
                            }.show()
                        } else if (BuildConfig.VERSION_CODE < versionLast) {
                            alert(getString(R.string.update_available), getString(R.string.update_title)) {
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

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}
