package br.com.gazoza.alcoolougasolina.activity

import android.content.IntentSender
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.application.CustomApplication
import br.com.gazoza.alcoolougasolina.databinding.ActivityStartBinding
import br.com.gazoza.alcoolougasolina.util.API_NOTIFICATIONS
import br.com.gazoza.alcoolougasolina.util.PARAM_ID
import br.com.gazoza.alcoolougasolina.util.PARAM_ITEM_ID
import br.com.gazoza.alcoolougasolina.util.PARAM_TYPE
import br.com.gazoza.alcoolougasolina.util.PREF_DEVICE_ID
import br.com.gazoza.alcoolougasolina.util.PREF_DEVICE_ID_OLD
import br.com.gazoza.alcoolougasolina.util.appLog
import br.com.gazoza.alcoolougasolina.util.hide
import br.com.gazoza.alcoolougasolina.util.isDebug
import br.com.gazoza.alcoolougasolina.util.isNotNumeric
import br.com.gazoza.alcoolougasolina.util.sendNotificationReport
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.orhanobut.hawk.Hawk
import org.jetbrains.anko.intentFor
import java.util.Calendar
import kotlin.random.Random

class StartActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SplashActivity"
        const val MY_REQUEST_CODE = 1
    }

    private lateinit var binding: ActivityStartBinding

    private var appUpdateManager: AppUpdateManager? = null
    private var alreadyStarted: Boolean = false

    private var updateFlowResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            appLog(TAG, "installSplashScreen() called")
        }
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createDeviceID()

        registerUpdateLauncher()

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
            ?.addOnFailureListener {
                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    requestUpdate(appUpdateInfo)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateFlowResultLauncher?.unregister()
        updateFlowResultLauncher = null
    }

    private fun registerUpdateLauncher() {
        updateFlowResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    initApp()
                } else {
                    binding.pbLoading.hide()

                    AlertDialog.Builder(this)
                        .setTitle(R.string.error_on_update)
                        .setMessage(R.string.error_on_update_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.try_again) { dialog, _ ->
                            checkAppVersion()

                            dialog.dismiss()
                        }
                        .setNegativeButton(R.string.update_later) { dialog, _ ->
                            initApp()

                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
    }

    private fun checkAppVersion() {
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        appUpdateManager
            ?.appUpdateInfo
            ?.addOnFailureListener {
                initApp()

                it.printStackTrace()
            }
            ?.addOnSuccessListener { appUpdateInfo ->
                val updateAvailable =
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isImmediate = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

                if (updateAvailable && isImmediate) {
                    requestUpdate(appUpdateInfo)
                } else {
                    initApp()
                }
            }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            val starter =
                IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
                    val request = IntentSenderRequest.Builder(intent)
                        .setFillInIntent(fillInIntent)
                        .setFlags(flagsValues, flagsMask)
                        .build()

                    updateFlowResultLauncher?.launch(request)
                }

            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                starter,
                MY_REQUEST_CODE,
            )
        } catch (e: IntentSender.SendIntentException) {
            if (isDebug()) e.printStackTrace()
        }
    }

    private fun initApp() {
        if (alreadyStarted) return

        alreadyStarted = true

        val id = intent.getStringExtra(PARAM_ID) ?: ""
        val type = intent.getStringExtra(PARAM_TYPE) ?: ""
        val itemId = intent.getStringExtra(PARAM_ITEM_ID) ?: ""
        appLog(TAG, "Received type from notification id: $id")
        appLog(TAG, "Received type from notification type: $type")
        appLog(TAG, "Received itemId from notification itemId: $itemId")

        if (id.isNotEmpty())
            sendNotificationReport(id, false)

        startActivity(intentFor<MainActivity>(PARAM_TYPE to type))

        if (type == API_NOTIFICATIONS)
            if (itemId.isEmpty())
                startActivity(intentFor<NotificationsActivity>())
            else
                startActivity(intentFor<NotificationDetailsActivity>(PARAM_ITEM_ID to itemId))

        finish()
    }

    private fun createDeviceID() {
        val currentDeviceID = Hawk.get(PREF_DEVICE_ID, "")
        val isNotNumeric = currentDeviceID.isNotNumeric()
        appLog(TAG, "createDeviceID() - currentDeviceID: $currentDeviceID")

        if (currentDeviceID.isEmpty() || isNotNumeric) {
            if (isNotNumeric) Hawk.put(PREF_DEVICE_ID_OLD, currentDeviceID)

            val milliseconds = Calendar.getInstance().timeInMillis.toString()
            val random = Random.nextInt(10000, 99999)
            var stringID = "$milliseconds$random"

            if (stringID.length > 18) {
                stringID = stringID.substring(0, 18)
            } else if (stringID.length < 18) {
                stringID = stringID.padEnd(18, '9')
            }

            Hawk.put(PREF_DEVICE_ID, stringID)
            CustomApplication().updateFuelParams()

            appLog("GENERATE_DEVICE_ID", "New device ID: $stringID")
        } else {
            appLog("GENERATE_DEVICE_ID", "Ignored, current ID: $currentDeviceID")
        }
    }

}
