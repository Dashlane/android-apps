package com.dashlane.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.inappautofill.AutoFillApiManager
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.navigation.Navigator
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.settings.general.backup.SettingsBackupViewModel
import com.dashlane.settings.search.SettingsSearchScreen
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsSearchActivity : DashlaneActivity() {
    @Inject
    lateinit var lockNavigationHelper: LockNavigationHelper

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var backupCoordinator: BackupCoordinator

    @Inject
    lateinit var autofillApiManager: AutoFillApiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val backupViewModel: SettingsBackupViewModel by viewModels()
        
        val (exportLauncher, importLauncher) = registerBackupContractListeners(
            backupCoordinator = backupCoordinator,
            backupViewModel = backupViewModel,
        )
        setContent {
            DashlaneTheme {
                Box(Modifier.safeDrawingPadding()) {
                    SettingsSearchScreen(
                        accountViewModel = hiltViewModel(),
                        advancedViewModel = hiltViewModel(),
                        mainMiscViewModel = hiltViewModel(),
                        applicationLockViewModel = hiltViewModel(),
                        miscViewModel = hiltViewModel(),
                        autofillViewModel = hiltViewModel(),
                        notificationsViewModel = hiltViewModel(),
                        backupViewModel = backupViewModel,
                        usageDataViewModel = hiltViewModel(),
                        helpViewModel = hiltViewModel(),
                        onBackPressed = ::finish,
                        applicationLockSideEffect = { checkApplicationLockSideEffect(lockNavigationHelper, it) },
                        miscSideEffect = { checkMiscSideEffect(lockNavigationHelper, it) },
                        accountSideEffect = ::checkAccountSideEffect,
                        advancedSideEffect = { checkAdvancedSideEffect(lockNavigationHelper, it) },
                        mainMiscSideEffect = { checkMainMiscSideEffect(navigator, toaster, it) },
                        autofillSideEffect = { checkAutofillSideEffect(it, autofillApiManager) },
                        notificationsSideEffect = ::checkNotificationsSideEffect,
                        backupSideEffect = { sideEffect ->
                            checkBackupSideEffect(
                                lockNavigationHelper = lockNavigationHelper,
                                backupCoordinator = backupCoordinator,
                                sideEffect = sideEffect,
                                dashExportLauncher = exportLauncher,
                                dashImportLauncher = importLauncher,
                            )
                        },
                        checkHelpSideEffect = { checkHelpSideEffect(toaster, it) },
                    )
                }
            }
        }
    }
}