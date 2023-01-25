package com.dashlane.autofill.api.unlockfill

import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObjectType



internal class SupportedAutofillsDetector {
    fun detectResponse(desktopId: Int?, formType: Int?): SupportedAutofills? =
        desktopId?.let(::detectResponseFromItemDataType) ?: formType?.let(::detectResponseFromFormType)

    private fun detectResponseFromItemDataType(desktopId: Int): SupportedAutofills? {
        return when (SyncObjectTypeUtils.valueFromDesktopIdIfExist(desktopId)) {
            SyncObjectType.AUTHENTIFIANT -> SupportedAutofills.AUTHENTIFIANT
            SyncObjectType.EMAIL -> SupportedAutofills.EMAIL
            SyncObjectType.PAYMENT_CREDIT_CARD -> SupportedAutofills.CREDIT_CARD
            else -> null
        }
    }

    private fun detectResponseFromFormType(formType: Int): SupportedAutofills? {
        return when (formType) {
            AutoFillFormType.CREDIT_CARD -> SupportedAutofills.CREDIT_CARD
            AutoFillFormType.CREDENTIAL, AutoFillFormType.USERNAME_ONLY -> SupportedAutofills.AUTHENTIFIANT
            AutoFillFormType.EMAIL_ONLY -> SupportedAutofills.EMAIL
            else -> null
        }
    }
}