package com.dashlane.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.autofill.actionssources.view.ActionsSourcesActivity
import com.dashlane.autofillapi.ChromeAutofillSettingsHelper.buildChromeAutofillSettingsIntent
import com.dashlane.csvexport.CsvExportActivity
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.inappautofill.AutoFillApiManager
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.premium.paywall.common.PaywallActivity
import com.dashlane.premium.paywall.common.PaywallActivityArgs
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.securearchive.contract.SecureArchiveExportResultContract
import com.dashlane.securearchive.contract.SecureArchiveFileSelectResultContract
import com.dashlane.securearchive.contract.SecureArchiveImportResultContract
import com.dashlane.settings.general.SettingsGeneralScreen
import com.dashlane.settings.general.autofill.SettingsAutofillState
import com.dashlane.settings.general.backup.SettingsBackupState
import com.dashlane.settings.general.backup.SettingsBackupViewModel
import com.dashlane.settings.general.notifications.SettingsNotificationsState
import com.dashlane.settings.ui.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.onboarding.ChromeInAppLoginIntroActivity
import com.dashlane.ui.activities.onboarding.InAppLoginIntroActivity
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState
import com.dashlane.util.compose.BackPressedDispatcherBridge
import com.dashlane.util.startActivityForResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsGeneralActivity : DashlaneActivity() {
    @Inject
    lateinit var lockNavigationHelper: LockNavigationHelper

    @Inject
    lateinit var backupCoordinator: BackupCoordinator

    @Inject
    lateinit var autofillApiManager: AutoFillApiManager

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)
        val backupViewModel: SettingsBackupViewModel by viewModels()
        
        val (exportLauncher, importLauncher) = registerBackupContractListeners(
            backupCoordinator = backupCoordinator,
            backupViewModel = backupViewModel,
        )

        setContent {
            DashlaneTheme {
                val topBarState by rememberSaveable { mutableStateOf(TopBarState(title = getString(R.string.settings_category_general))) }
                TopBarScaffold(
                    topBarState = topBarState,
                    back = { finish() },
                ) { contentPadding ->

                    SettingsGeneralScreen(
                        modifier = Modifier.padding(contentPadding),
                        autofillViewModel = hiltViewModel(),
                        notificationsViewModel = hiltViewModel(),
                        backupViewModel = backupViewModel,
                        usageDataViewModel = hiltViewModel(),
                        autofillSideEffect = { sideEffect -> checkAutofillSideEffect(sideEffect, autofillApiManager) },
                        notificationsSideEffect = { sideEffect -> checkNotificationsSideEffect(sideEffect) },
                        backupSideEffect = { sideEffect ->
                            checkBackupSideEffect(
                                lockNavigationHelper = lockNavigationHelper,
                                backupCoordinator = backupCoordinator,
                                sideEffect = sideEffect,
                                dashExportLauncher = exportLauncher,
                                dashImportLauncher = importLauncher,
                            )
                        },
                    )
                }
            }
        }
    }
}

internal fun Activity.checkAutofillSideEffect(
    sideEffect: SettingsAutofillState.SideEffect,
    autofillApiManager: AutoFillApiManager,
) {
    when (sideEffect) {
        SettingsAutofillState.SideEffect.GoToAutofillSettings -> {
            autofillApiManager.createAutofillSettingsPendingIntent(false).send()
        }
        is SettingsAutofillState.SideEffect.GoToChromeAutofillSettings -> {
            startActivity(buildChromeAutofillSettingsIntent(sideEffect.packageName))
        }
        SettingsAutofillState.SideEffect.GoToInAppLogin -> {
            startActivity(Intent(this, InAppLoginIntroActivity::class.java))
        }
        SettingsAutofillState.SideEffect.GoToChromeInAppLogin -> {
            startActivity(Intent(this, ChromeInAppLoginIntroActivity::class.java))
        }
        SettingsAutofillState.SideEffect.GoToPausedAutofill -> {
            startActivity(Intent(this, ActionsSourcesActivity::class.java))
        }
        SettingsAutofillState.SideEffect.GoToPaywall -> {
            val args = PaywallActivityArgs(paywallIntroType = PaywallIntroType.FROZEN_ACCOUNT).toBundle()
            startActivity(Intent(this, PaywallActivity::class.java).apply { putExtras(args) })
        }
    }
}

internal fun Activity.checkNotificationsSideEffect(sideEffect: SettingsNotificationsState.SideEffect) {
    when (sideEffect) {
        SettingsNotificationsState.SideEffect.GoToSystemSettings -> {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, application.packageName)
            startActivity(intent)
        }
    }
}

internal fun DashlaneActivity.registerBackupContractListeners(
    backupCoordinator: BackupCoordinator,
    backupViewModel: SettingsBackupViewModel,
): Pair<ActivityResultLauncher<Unit>, ActivityResultLauncher<Unit>> {
    val retryImport = { backupViewModel.sendEvent(SettingsEvent.ItemClicked(Backup.Import)) }
    val exportLauncher = registerForActivityResult(SecureArchiveExportResultContract()) { result ->
        backupCoordinator.handleExportResult(
            activity = this,
            exportResult = result,
            onRetryExport = { backupViewModel.sendEvent(SettingsEvent.ItemClicked(Backup.Export)) },
        )
    }
    val importLauncher = registerForActivityResult(SecureArchiveImportResultContract()) { result ->
        backupCoordinator.handleImportResult(
            activity = this,
            importResult = result,
            onRetryImport = retryImport,
        )
    }
    val chooseDashFileLauncher = registerForActivityResult(SecureArchiveFileSelectResultContract()) { uri ->
        backupCoordinator.handleChooseFileResult(
            activity = this,
            uri = uri,
            onValidDashFile = { validUri -> importLauncher.launch(validUri) },
            onRetryImport = retryImport,
        )
    }

    return exportLauncher to chooseDashFileLauncher
}

internal fun ComponentActivity.checkBackupSideEffect(
    lockNavigationHelper: LockNavigationHelper,
    backupCoordinator: BackupCoordinator,
    sideEffect: SettingsBackupState.SideEffect,
    dashExportLauncher: ActivityResultLauncher<Unit>,
    dashImportLauncher: ActivityResultLauncher<Unit>,
) {
    when (sideEffect) {
        is SettingsBackupState.SideEffect.DisplayLock -> displayLock(
            lockNavigationHelper = lockNavigationHelper,
            forceMasterPassword = sideEffect.forceMasterPassword,
        )
        SettingsBackupState.SideEffect.GoToExportCsv -> {
            startActivityForResult<CsvExportActivity>(CsvExportActivity.REQUEST_CODE)
        }
        SettingsBackupState.SideEffect.GoToImport -> {
            
            backupCoordinator.onImportStarted()
            dashImportLauncher.launch(Unit)
        }
        SettingsBackupState.SideEffect.StartExport -> {
            backupCoordinator.onExportStarted()
            dashExportLauncher.launch(Unit)
        }
    }
}