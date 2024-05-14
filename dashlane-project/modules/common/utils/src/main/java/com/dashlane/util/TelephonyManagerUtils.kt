@file:JvmName("TelephonyManagerUtils")

package com.dashlane.util

import android.telephony.TelephonyManager
import java.util.Locale

val TelephonyManager.deviceCountry: String?
    get() = simCountryIso?.takeIf { it.length == 2 }?.lowercase(Locale.US) ?: deviceCountryByNetwork

private val TelephonyManager.deviceCountryByNetwork: String?
    get() = phoneType.takeIf { it != TelephonyManager.PHONE_TYPE_CDMA }?.let {
        
        networkCountryIso?.takeIf { it.length == 2 }?.lowercase(Locale.US)
    }