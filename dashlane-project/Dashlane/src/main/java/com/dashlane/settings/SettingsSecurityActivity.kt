package com.dashlane.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.accountrecoverykey.AccountRecoveryKeyActivity
import com.dashlane.biometric.BiometricSetupActivity
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.disabletotp.DisableTotpActivity
import com.dashlane.disabletotp.DisableTotpEnforcedIntroActivity
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.masterpassword.ChangeMasterPasswordComposeActivity
import com.dashlane.pin.settings.PinSettingsActivity
import com.dashlane.settings.security.SettingsSecurityScreen
import com.dashlane.settings.security.applicationlock.SettingsApplicationLockState
import com.dashlane.settings.security.misc.SettingsMiscState
import com.dashlane.settings.ui.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState
import com.dashlane.ui.screens.settings.DevicesActivity
import com.dashlane.util.clearTop
import com.dashlane.util.compose.BackPressedDispatcherBridge
import com.dashlane.util.launchUrl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsSecurityActivity : DashlaneActivity() {
    @Inject
    lateinit var lockNavigationHelper: LockNavigationHelper

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)

        setContent {
            DashlaneTheme {
                val topBarState by rememberSaveable { mutableStateOf(TopBarState(title = getString(R.string.settings_category_security))) }

                TopBarScaffold(
                    topBarState = topBarState,
                    back = ::finish,
                ) { contentPadding ->
                    SettingsSecurityScreen(
                        modifier = Modifier.padding(contentPadding),
                        applicationLockViewModel = hiltViewModel(),
                        miscViewModel = hiltViewModel(),
                        applicationLockSideEffect = { checkApplicationLockSideEffect(lockNavigationHelper, it) },
                        miscSideEffect = { checkMiscSideEffect(lockNavigationHelper, it) },
                    )
                }
            }
        }
    }
}

internal fun Activity.checkApplicationLockSideEffect(
    lockNavigationHelper: LockNavigationHelper,
    sideEffect: SettingsApplicationLockState.SideEffect,
) {
    when (sideEffect) {
        is SettingsApplicationLockState.SideEffect.DisplayLock -> displayLock(lockNavigationHelper, sideEffect.forceMasterPassword)
        SettingsApplicationLockState.SideEffect.GoToPinLock -> {
            startActivity(Intent(this, PinSettingsActivity::class.java))
        }
        SettingsApplicationLockState.SideEffect.GoToARK -> {
            val intent = Intent(this, AccountRecoveryKeyActivity::class.java)
            intent.putExtra("showIntro", false)
            intent.putExtra("userCanExitFlow", true)
            startActivity(intent)
        }
        SettingsApplicationLockState.SideEffect.GoToBiometricLock -> {
            startActivity(BiometricSetupActivity.newIntent(context = this, isBiometricRecovery = false).clearTop())
        }
        SettingsApplicationLockState.SideEffect.GoToBiometricRecovery -> {
            startActivity(BiometricSetupActivity.newIntent(context = this, isBiometricRecovery = true).clearTop())
        }
        SettingsApplicationLockState.SideEffect.GoToDisable2FA -> {
            startActivity(Intent(this, DisableTotpActivity::class.java).clearTop())
        }
        SettingsApplicationLockState.SideEffect.GoToDisable2FAEnforced -> {
            startActivity(Intent(this, DisableTotpEnforcedIntroActivity::class.java).clearTop())
        }
    }
}

internal fun Activity.checkMiscSideEffect(lockNavigationHelper: LockNavigationHelper, sideEffect: SettingsMiscState.SideEffect) {
    when (sideEffect) {
        is SettingsMiscState.SideEffect.DisplayLock -> displayLock(lockNavigationHelper, sideEffect.forceMasterPassword)
        is SettingsMiscState.SideEffect.GoToChangeMasterPassword -> {
            startActivity(
                ChangeMasterPasswordComposeActivity.newIntent(
                    context = this,
                    origin = ChangeMasterPasswordOrigin.Settings,
                    showWarningDesktop = sideEffect.showWarningDesktop,
                ),
            )
        }
        SettingsMiscState.SideEffect.GoToManageDevices -> {
            startActivity(Intent(this, DevicesActivity::class.java))
        }
        is SettingsMiscState.SideEffect.GoToPrivacy -> launchUrl(sideEffect.uri)
    }
}