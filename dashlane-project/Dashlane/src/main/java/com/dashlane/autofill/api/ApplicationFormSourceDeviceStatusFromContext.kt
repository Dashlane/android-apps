package com.dashlane.autofill.api

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.dashlane.autofill.internal.ApplicationFormSourceDeviceStatus
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.core.helpers.PackageSignatureStatus
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.util.getApplicationInfoCompat
import com.dashlane.util.tryOrNull
import javax.inject.Inject

class ApplicationFormSourceDeviceStatusFromContext @Inject constructor(
    private val packageManager: PackageManager,
    private val knownApplicationProvider: KnownApplicationProvider,
    private val packageNameSignatureHelper: PackageNameSignatureHelper
) : ApplicationFormSourceDeviceStatus {

    override fun getApplicationInfo(applicationFormSource: ApplicationFormSource): ApplicationInfo? {
        return tryOrNull { packageManager.getApplicationInfoCompat(applicationFormSource.packageName, 0) }
    }

    override fun getSignatureAssessment(applicationFormSource: ApplicationFormSource): PackageSignatureStatus {
        return knownApplicationProvider.getKnownApplication(applicationFormSource.packageName)?.let {
            packageNameSignatureHelper.getPackageNameSignatureVerificationStatus(
                applicationFormSource.packageName,
                null,
                it.mainDomain
            )
        } ?: PackageSignatureStatus.UNKNOWN
    }
}
