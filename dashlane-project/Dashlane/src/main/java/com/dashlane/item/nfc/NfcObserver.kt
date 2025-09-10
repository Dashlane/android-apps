package com.dashlane.item.nfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.dashlane.util.tryOrNull
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NfcObserver(private val activity: ComponentActivity) {
    val nfcAdapter: NfcAdapter?
        get() = tryOrNull { NfcAdapter.getDefaultAdapter(activity) }

    fun enableReader(): Flow<Tag> = callbackFlow {
        val listener = { tag: Tag? ->
            if (tag != null) {
                trySend(tag)
            }
        }
        nfcAdapter?.enableReaderMode(activity, listener, FLAGS, options)

        awaitClose {
            nfcAdapter?.disableReaderMode(activity)
            close()
        }
    }

    companion object {
        private const val FLAGS = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NFC_BARCODE or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        private val options: Bundle = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }
    }
}