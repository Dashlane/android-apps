package com.dashlane.autofill.pause.presenter

import android.content.Context
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.internal.ApplicationFormSourceDeviceStatus
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.util.formSourceIdentifier
import com.dashlane.core.helpers.PackageSignatureStatus
import com.dashlane.url.toUrlOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AutofillFormSourceProvider @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val applicationFormSourceDeviceStatus: ApplicationFormSourceDeviceStatus,
) {
    fun getIcon(form: AutoFillFormSource): AutofillFormSourceIcon =
        when (form) {
            is ApplicationFormSource -> getApplicationIcon(form)
            is WebDomainFormSource -> AutofillFormSourceIcon.WebsiteUrl(form.webDomain)
        }

    private fun getApplicationIcon(form: ApplicationFormSource): AutofillFormSourceIcon {
        val applicationInfo = applicationFormSourceDeviceStatus.getApplicationInfo(form)
            ?: return AutofillFormSourceIcon.NotInstalledApplication()

        val signatureAssessment = applicationFormSourceDeviceStatus.getSignatureAssessment(form)
        if (signatureAssessment == PackageSignatureStatus.INCORRECT) {
            return AutofillFormSourceIcon.UnknownApplication
        }

        return AutofillFormSourceIcon.InstalledApplication(applicationInfo)
    }

    fun getServiceName(form: AutoFillFormSource): String =
        when (form) {
            is ApplicationFormSource -> getApplicationName(form)
            is WebDomainFormSource -> getWebsiteName(form)
        }

    private fun getApplicationName(form: ApplicationFormSource): String {
        val applicationInfo = applicationFormSourceDeviceStatus.getApplicationInfo(form)
            ?: return form.formSourceIdentifier

        val signatureAssessment = applicationFormSourceDeviceStatus.getSignatureAssessment(form)
        if (signatureAssessment == PackageSignatureStatus.INCORRECT) {
            return form.formSourceIdentifier
        }

        return context.packageManager.getApplicationLabel(applicationInfo).toString()
    }

    private fun getWebsiteName(form: WebDomainFormSource) =
        form.webDomain.toUrlOrNull()?.topPrivateDomain() ?: form.webDomain
}