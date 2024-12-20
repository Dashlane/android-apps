package com.dashlane.accountrecoverykey

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.navArgs
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.DetailSettings
import com.dashlane.accountrecoverykey.AccountRecoveryKeyDestination.Intro
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.compose.BackPressedDispatcherBridge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountRecoveryKeyActivity : DashlaneActivity() {

    val args: AccountRecoveryKeyActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startDestination = if (args.showIntro) Intro else DetailSettings
        val userCanExitFlow: Boolean = args.userCanExitFlow

        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)

        setContent {
            DashlaneTheme {
                AccountRecoveryKeySettingsNavigation(
                    startDestination = startDestination,
                    userCanExitFlow = userCanExitFlow,
                    onCancel = { finish() }
                )
            }
        }
    }
}
