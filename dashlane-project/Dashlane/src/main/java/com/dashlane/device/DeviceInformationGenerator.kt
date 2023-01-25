package com.dashlane.device

import android.content.Context
import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.crashreport.CrashReporter
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.storage.DataStorageProvider
import com.dashlane.util.PackageUtilities
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter



class DeviceInformationGenerator(
    private val context: Context,
    private val crashReporter: CrashReporter,
    private val accountRecovery: AccountRecovery,
    private val inAppLoginManager: InAppLoginManager,
    private val dataStorageProvider: DataStorageProvider
) {

    fun generate() = DeviceInformation(
        crashReportId = crashReporter.crashReporterId,
        dashlaneAppSignature = PackageUtilities.getSignatures(
            context,
            context.packageName
        )?.sha256Signatures?.joinToString(),
        installerOrigin = PackageUtilities.getInstallerOrigin(context),
        hasMPReset = accountRecovery.isFeatureEnabled.toString(),
        appInstallDate = PackageUtilities.getAppPackageInfo(context)?.firstInstallTime?.toZonedDateTime(),
        autofillEnabled = inAppLoginManager.isEnableForApp().toString(),
        racletteDatabase = dataStorageProvider.useRaclette.toString()
    )

    private fun Long.toZonedDateTime(): String =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}