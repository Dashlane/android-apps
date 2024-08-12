package com.dashlane.ui.screens.settings.list.general

import android.content.Context
import com.dashlane.R
import com.dashlane.navigation.Navigator
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.inject.OptionalProvider

class SettingsGeneralBackupList(
    context: Context,
    dialogHelper: DialogHelper,
    private val backupCoordinator: BackupCoordinator,
    private val sensibleSettingsClickHelper: SensibleSettingsClickHelper,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val navigator: Navigator
) {
    private val backupHeader =
        SettingHeader(context.getString(R.string.setting_backup_category))

    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()

    private val isExportDisabledByTeam: Boolean
        get() = teamSpaceAccessor?.isVaultExportEnabled == false

    private val isForcedDomainsEnabledByTeam: Boolean
        get() = teamSpaceAccessor?.isForcedDomainsEnabled == true

    private val backupExportItem = object : SettingItem {
        override val id = "backup-export"
        override val header = backupHeader
        override val title = context.getString(R.string.setting_backup_export)
        override val description = context.getString(R.string.setting_backup_export_description)
        override fun isEnable() = !isExportDisabledByTeam
        override fun isVisible() = true
        override fun onClick(context: Context) {
            when {
                isEnable() && isForcedDomainsEnabledByTeam ->
                    dialogHelper
                        .builder(context, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
                        .setTitle(R.string.settings_general_backup_export_business_data_restriction_title)
                        .setView(R.layout.dialog_export_business_data_restriction)
                        .setPositiveButton(R.string.ok) { _, _ -> askPasswordAndStartExport(context = context) }
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                        .apply { initializeViewTreeOwners() }
                        .show()
                isEnable() -> askPasswordAndStartExport(context = context)
                isExportDisabledByTeam ->
                    dialogHelper
                        .builder(context, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
                        .setTitle(R.string.settings_general_backup_export_restriction_title)
                        .setMessage(R.string.settings_general_backup_export_restriction_description)
                        .setPositiveButton(R.string.ok, null)
                        .show()
            }
        }
    }

    private val backupCsvExportItem = object : SettingItem {
        override val id = "backup-export-csv"
        override val header = backupHeader
        override val title = context.getString(R.string.setting_backup_export_csv)
        override val description = context.getString(R.string.setting_backup_export_csv_description)
        override fun isEnable() = backupExportItem.isEnable()
        override fun isVisible() = backupExportItem.isVisible()
        override fun onClick(context: Context) {
            if (!isEnable()) {
                return
            }
            navigator.goToGuidedWebCsvExport()
        }
    }

    private val backupImportItem = object : SettingItem {
        override val id = "backup-import"
        override val header = backupHeader
        override val title = context.getString(R.string.setting_backup_import)
        override val description = context.getString(R.string.setting_backup_import_description)
        override fun isEnable() = true
        override fun isVisible() = backupExportItem.isVisible()
        override fun onClick(context: Context) = backupCoordinator.startImport()
    }

    private fun askPasswordAndStartExport(context: Context) {
        sensibleSettingsClickHelper.perform(
            context = context,
            forceMasterPassword = true
        ) {
            backupCoordinator.startExport()
        }
    }

    fun getAll() = listOf(
        backupExportItem,
        backupCsvExportItem,
        backupImportItem
    )
}