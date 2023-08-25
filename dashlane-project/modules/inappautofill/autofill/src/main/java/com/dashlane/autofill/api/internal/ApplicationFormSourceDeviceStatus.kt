package com.dashlane.autofill.api.internal

import android.content.pm.ApplicationInfo
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.core.helpers.PackageSignatureStatus

interface ApplicationFormSourceDeviceStatus {
    fun getApplicationInfo(applicationFormSource: ApplicationFormSource): ApplicationInfo?
    fun getSignatureAssessment(applicationFormSource: ApplicationFormSource): PackageSignatureStatus
}
