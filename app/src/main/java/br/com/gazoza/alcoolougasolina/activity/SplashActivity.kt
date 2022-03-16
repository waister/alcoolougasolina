package br.com.gazoza.alcoolougasolina.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.application.CustomApplication
import br.com.gazoza.alcoolougasolina.util.*
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.intentFor

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SplashActivity"
        const val MY_REQUEST_CODE = 1
    }

    private var appUpdateManager: AppUpdateManager? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Hawk.get(PREF_DEVICE_ID, "").isEmpty()) {
            val id = Settings.Secure.ANDROID_ID
            Hawk.put(PREF_DEVICE_ID, Settings.Secure.getString(contentResolver, id))

            CustomApplication().updateFuelParams()
        }

        if ((application as CustomApplication).getIsCheckUpdatesNeeded()) {
            (application as CustomApplication).setCheckUpdatesIsNeeded(false)

            checkAppVersion()
        } else {
            initApp()
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener {
                if (it.updateAvailability() == DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                    appUpdateManager?.startUpdateFlowForResult(
                        it, IMMEDIATE, this, MY_REQUEST_CODE
                    )
            }
    }

    private fun checkAppVersion() {
        ll_loading.visibility = View.VISIBLE

        appUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

        appUpdateInfoTask
            ?.addOnFailureListener {
                initApp()
            }
            ?.addOnSuccessListener {
                val updateAvailable = it.updateAvailability() == UPDATE_AVAILABLE
                val isImmediate = it.isUpdateTypeAllowed(IMMEDIATE)

                if (updateAvailable && isImmediate)
                    appUpdateManager?.startUpdateFlowForResult(
                        it, IMMEDIATE, this, MY_REQUEST_CODE
                    )
                else
                    initApp()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                initApp()
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

    private fun initApp() {
        val type = intent.getStringExtra(PARAM_TYPE)
        val itemId = intent.getStringExtra(PARAM_ITEM_ID)

        appLog(TAG, "Received type from notification: $type")
        appLog(TAG, "Received itemId from notification: $itemId")

        startActivity(intentFor<MainActivity>(PARAM_TYPE to type))

        if (type == API_NOTIFICATIONS)
            if (itemId == null || itemId.isEmpty())
                startActivity(intentFor<NotificationsActivity>())
            else
                startActivity(intentFor<NotificationDetailsActivity>(PARAM_ITEM_ID to itemId))

        finish()
    }

}
