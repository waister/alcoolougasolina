package br.com.gazoza.alcoolougasolina.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.application.CustomApplication
import br.com.gazoza.alcoolougasolina.util.*
import com.explorestack.consent.*
import com.explorestack.consent.exception.ConsentManagerException
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.intentFor

class SplashActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SplashActivity"
        const val MY_REQUEST_CODE = 1
    }

    private var appUpdateManager: AppUpdateManager? = null
    private var consentForm: ConsentForm? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Hawk.get(PREF_DEVICE_ID, "").isEmpty()) {
            Hawk.put(
                PREF_DEVICE_ID,
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            )
            CustomApplication().updateFuelParams()
        }

        if ((application as CustomApplication).getIsCheckUpdatesNeeded()) {
            (application as CustomApplication).setCheckUpdatesIsNeeded(false)

            checkAppVersion()
        } else {
            resolveUserConsent()
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                }
            }
    }

    private fun checkAppVersion() {
        ll_loading.visibility = View.VISIBLE

        appUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

        appUpdateInfoTask
            ?.addOnFailureListener {
                resolveUserConsent()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                val updateAvailable =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isImmediate = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

                if (updateAvailable && isImmediate) {

                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                } else {

                    resolveUserConsent()

                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                resolveUserConsent()
            } else {
                ll_loading.visibility = View.GONE

                val dialog = AlertDialog.Builder(this)
                dialog.setCancelable(false)
                dialog.setTitle(R.string.error_on_update)
                dialog.setMessage(R.string.error_on_update_message)
                dialog.setPositiveButton(R.string.try_again) { _, _ ->
                    checkAppVersion()
                }
                dialog.setNegativeButton(R.string.exit_app) { _, _ ->
                    finish()
                }
                dialog.create()
                dialog.show()
            }
        }
    }

    private fun resolveUserConsent() {
        val consentManager = ConsentManager.getInstance(this)

        consentManager.requestConsentInfoUpdate(APPODEAL_APP_KEY, object :
            ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consent: Consent) {
                val consentShouldShow = consentManager.shouldShowConsentDialog()
                if (consentShouldShow == Consent.ShouldShow.TRUE) {
                    showConsentForm()
                } else {
                    initApp()
                }
            }

            override fun onFailedToUpdateConsentInfo(e: ConsentManagerException) {
                initApp()
            }
        })
    }

    private fun showConsentForm() {
        if (consentForm == null) {
            consentForm = ConsentForm.Builder(this).withListener(object : ConsentFormListener {
                override fun onConsentFormLoaded() {
                    consentForm!!.showAsActivity()
                }

                override fun onConsentFormError(error: ConsentManagerException) {
                    initApp()
                }

                override fun onConsentFormOpened() {}

                override fun onConsentFormClosed(consent: Consent) {
                    if (consent.status == Consent.Status.PERSONALIZED)
                        initApp()
                    else
                        finish()
                }
            }).build()
        }

        if (consentForm != null) {
            if (consentForm!!.isLoaded) {
                consentForm!!.showAsActivity()
            } else {
                consentForm!!.load()
            }
        }
    }

    private fun initApp() {
        val type = intent.getStringExtra(PARAM_TYPE)
        val itemId = intent.getStringExtra(PARAM_ITEM_ID)
        Log.i(TAG, "Received type from notification: $type")
        Log.i(TAG, "Received itemId from notification: $itemId")

        startActivity(intentFor<MainActivity>(PARAM_TYPE to type))

        when (type) {
            API_NOTIFICATIONS -> {
                if (itemId == null || itemId.isEmpty())
                    startActivity(intentFor<NotificationsActivity>())
                else
                    startActivity(intentFor<NotificationDetailsActivity>(PARAM_ITEM_ID to itemId))
            }
        }

        finish()
    }

}
