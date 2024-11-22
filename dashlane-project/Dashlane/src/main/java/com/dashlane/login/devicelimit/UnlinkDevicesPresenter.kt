package com.dashlane.login.devicelimit

import android.app.Activity
import android.os.Bundle
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.login.Device
import com.dashlane.login.LoginIntents
import com.dashlane.login.devicelimit.UnlinkDevicesActivity.Companion.EXTRA_DEVICES
import com.dashlane.login.progress.LoginSyncProgressActivity
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.util.getParcelableArrayCompat
import com.dashlane.util.setCurrentPageView
import javax.inject.Inject
import com.dashlane.hermes.generated.events.user.CallToAction as UserCallToAction

class UnlinkDevicesPresenter @Inject constructor(
    private val activity: Activity,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val logRepository: LogRepository
) : UnlinkDevicesContract.Presenter {

    override var viewProxy: UnlinkDevicesContract.ViewProxy? = null
    private lateinit var devices: List<Device>

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    override fun onCreate(savedInstanceState: Bundle?) {
        val myDeviceId = sessionManager.session?.accessKey
        if (myDeviceId == null) {
            activity.finish()
            return
        }
        val extraDevices: List<Device>? = savedInstanceState?.getParcelableArrayCompat(EXTRA_DEVICES)
            ?: activity.intent.getParcelableArrayCompat(EXTRA_DEVICES)
        if (extraDevices == null) {
            activity.finish()
            return
        }
        devices = extraDevices
            .filterNot { it.id == myDeviceId }
            .sortedByDescending { it.lastActivityDate }
        if (savedInstanceState == null) {
            activity.setCurrentPageView(page = AnyPage.PAYWALL_DEVICE_SYNC_LIMIT_UNLINK_DEVICE)
        }
        viewProxy?.showDevices(devices)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray(EXTRA_DEVICES, devices.toTypedArray())
    }

    override fun onUnlink(selectedDevices: List<Device>) {
        userPreferencesManager.isOnLoginPaywall = false
        logUserAction(chosenAction = CallToAction.UNLINK)
        val intent = LoginIntents.createSettingsActivityIntent(activity, false).apply {
            putExtra(
                LoginSyncProgressActivity.EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION,
                selectedDevices.toTypedArray()
            )
        }
        activity.startActivity(intent)
        activity.finish()
    }

    override fun onCancelUnlink() {
        logUserAction(chosenAction = null)
        activity.finish()
    }

    override fun onBackPressed() {
        logUserAction(chosenAction = null)
    }

    private fun logUserAction(chosenAction: CallToAction?) =
        logRepository.queueEvent(
            UserCallToAction(
                callToActionList = listOf(CallToAction.UNLINK),
                hasChosenNoAction = chosenAction == null,
                chosenAction = chosenAction
            )
        )
}
