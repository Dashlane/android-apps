package com.dashlane.accountsummary

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.lock.LockEvent.Unlock.Reason.WithCode
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockPrompt
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper.Companion.UNLOCK_EVENT_CODE
import com.dashlane.util.compose.BackPressedDispatcherBridge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountSummaryActivity : DashlaneActivity() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var lockNavigationHelper: LockNavigationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)

        setContent {
            DashlaneTheme {
                AccountSummaryNavigation(
                    goToLock = {
                        lockNavigationHelper.showLockActivityForReason(
                            context = this,
                            reason = WithCode(requestCode = UNLOCK_EVENT_CODE, origin = WithCode.Origin.EDIT_SETTINGS),
                            lockPrompt = LockPrompt.ForSettings(forceMasterPassword = false),
                        )
                    },
                    onCancel = { finish() },
                    onSuccess = { finish() },
                    onSuccessLogout = {
                        navigator.logoutAndCallLoginScreen(this, allowSkipEmail = true)
                    },
                )
            }
        }
    }
}