package com.dashlane.security

import com.dashlane.breach.Breach
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.SecurityAlertType

fun Breach.getAlertTypeForLogs() =
    if (isDarkWebBreach()) SecurityAlertType.DARK_WEB else SecurityAlertType.PUBLIC_BREACH

fun Breach.getItemTypesForLogs() = leakedData?.mapNotNull {
    when (it) {
        Breach.DATA_ADDRESS -> ItemType.ADDRESS
        Breach.DATA_CREDIT_CARD -> ItemType.CREDIT_CARD
        Breach.DATA_EMAIL -> ItemType.EMAIL
        Breach.DATA_PASSWORD, Breach.DATA_USERNAME -> ItemType.CREDENTIAL
        Breach.DATA_PERSONAL_INFORMATION -> ItemType.IDENTITY
        Breach.DATA_PHONE -> ItemType.PHONE
        Breach.DATA_SSN -> ItemType.SOCIAL_SECURITY
        else -> null
    }
}?.distinct() ?: emptyList()