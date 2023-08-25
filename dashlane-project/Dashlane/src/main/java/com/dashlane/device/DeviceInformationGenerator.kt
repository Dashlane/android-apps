package com.dashlane.device

import android.content.Context
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.crashreport.CrashReporter
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.util.PackageUtilities
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DeviceInformationGenerator(
    private val context: Context,
    private val crashReporter: CrashReporter,
    private val biometricRecovery: BiometricRecovery,
    private val inAppLoginManager: InAppLoginManager
) {

    fun generate() = DeviceInformation(
        crashReportId = crashReporter.crashReporterId,
        dashlaneAppSignature = PackageUtilities.getSignatures(
            context,
            context.packageName
        )?.sha256Signatures?.joinToString(),
        installerOrigin = PackageUtilities.getInstallerOrigin(context),
        hasMPReset = biometricRecovery.isFeatureEnabled.toString(),
        appInstallDate = PackageUtilities.getAppPackageInfo(context)?.firstInstallTime?.toZonedDateTime(),
        autofillEnabled = inAppLoginManager.isEnableForApp().toString(),
        racletteDatabase = true.toString()
    )

    private fun Long.toZonedDateTime(): String =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
}