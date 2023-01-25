package com.dashlane.autofill.api

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.dashlane.autofill.api.internal.ApplicationFormSourceDeviceStatus
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.core.helpers.PackageNameSignatureHelper
import com.dashlane.core.helpers.PackageSignatureStatus
import com.dashlane.ext.application.KnownApplication
import com.dashlane.util.getApplicationInfoCompat
import com.dashlane.util.tryOrNull
import javax.inject.Inject
import javax.inject.Provider



class ApplicationFormSourceDeviceStatusFromContext @Inject constructor(
    private val packageManagerProvider: Provider<PackageManager>
) : ApplicationFormSourceDeviceStatus {

    val packageManager: PackageManager
        get() = packageManagerProvider.get()

    override fun getApplicationInfo(applicationFormSource: ApplicationFormSource): ApplicationInfo? {
        return tryOrNull { packageManager.getApplicationInfoCompat(applicationFormSource.packageName, 0) }
    }

    override fun getSignatureAssessment(applicationFormSource: ApplicationFormSource): PackageSignatureStatus {
        return KnownApplication.getKnownApplication(applicationFormSource.packageName)?.let {
            PackageNameSignatureHelper()
                .getPackageNameSignatureVerificationStatus(
                    packageManager,
                    applicationFormSource.packageName,
                    null,
                    it.mainDomain
                )
        } ?: PackageSignatureStatus.UNKNOWN
    }
}
