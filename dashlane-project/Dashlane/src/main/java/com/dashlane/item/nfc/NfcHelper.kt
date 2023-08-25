package com.dashlane.item.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.os.Build
import com.dashlane.util.tryOrNull

class NfcHelper(private val activity: Activity) {

    val nfcAdapter: NfcAdapter?
        get() = tryOrNull { NfcAdapter.getDefaultAdapter(activity) }

    private val pendingIntent: PendingIntent = PendingIntent.getActivity(
        activity,
        0,
        Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
    )

    fun disableDispatch() = nfcAdapter?.disableForegroundDispatch(activity)

    fun enableDispatch() = nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, INTENT_FILTER, TECH_LIST)

    companion object {
        fun isNfcAvailable(context: Context): Boolean =
            runCatching { return NfcAdapter.getDefaultAdapter(context) != null }.getOrDefault(false)

        fun isNfcEnabled(context: Context): Boolean =
            runCatching { return NfcAdapter.getDefaultAdapter(context)?.isEnabled ?: false }.getOrDefault(false)

        private val INTENT_FILTER = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

        private val TECH_LIST = arrayOf(arrayOf(IsoDep::class.java.name))
    }
}
