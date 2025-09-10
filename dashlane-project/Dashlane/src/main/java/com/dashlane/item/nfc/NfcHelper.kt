package com.dashlane.item.nfc

import android.content.Context
import android.nfc.NfcAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NfcHelper @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    fun isNfcAvailable(): Boolean =
        runCatching { return NfcAdapter.getDefaultAdapter(context) != null }.getOrDefault(false)

    fun isNfcEnabled(): Boolean =
        runCatching {
            return NfcAdapter.getDefaultAdapter(context)?.isEnabled == true
        }.getOrDefault(false)
}