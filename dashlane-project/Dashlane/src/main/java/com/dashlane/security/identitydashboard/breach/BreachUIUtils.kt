package com.dashlane.security.identitydashboard.breach

import android.content.Context
import com.dashlane.R
import com.dashlane.breach.Breach
import com.dashlane.util.capitalizeToDisplay

fun Breach.getDataInvolvedFormatted(context: Context, exclude: Set<String> = emptySet()): String {
    return (leakedData ?: listOf())
        .filter { it !in exclude }
        .mapNotNull { toStringLeakDataType(context, it) }
        .joinToString(", ")
        .capitalizeToDisplay()
}

private fun toStringLeakDataType(context: Context, leakDataType: String): String? {
    return when (leakDataType) {
        Breach.DATA_USERNAME -> context.getString(R.string.breach_data_involved_username)
        Breach.DATA_PASSWORD -> context.getString(R.string.breach_data_involved_password)
        Breach.DATA_EMAIL -> context.getString(R.string.breach_data_involved_email)
        Breach.DATA_CREDIT_CARD -> context.getString(R.string.breach_data_involved_credit_card)
        Breach.DATA_PHONE -> context.getString(R.string.breach_data_involved_phone)
        Breach.DATA_ADDRESS -> context.getString(R.string.breach_data_involved_address)
        Breach.DATA_SSN -> context.getString(R.string.breach_data_involved_ssn)
        Breach.DATA_IP -> context.getString(R.string.breach_data_involved_ip)
        Breach.DATA_LOCATION -> context.getString(R.string.breach_data_involved_location)
        Breach.DATA_PERSONAL_INFORMATION -> context.getString(R.string.breach_data_involved_personal_information)
        Breach.DATA_SOCIAL_NETWORK -> context.getString(R.string.breach_data_involved_social_network)

        else -> null
    }
}