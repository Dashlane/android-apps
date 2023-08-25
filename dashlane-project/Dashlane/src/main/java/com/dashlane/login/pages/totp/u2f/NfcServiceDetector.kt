package com.dashlane.login.pages.totp.u2f

import android.nfc.tech.IsoDep

interface NfcServiceDetector {

    val isNfcAvailable: Boolean

    suspend fun detectNfcTag(): IsoDep
}