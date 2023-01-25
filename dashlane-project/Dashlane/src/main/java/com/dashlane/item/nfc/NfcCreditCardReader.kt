package com.dashlane.item.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.dashlane.util.getParcelableExtraCompat
import com.github.devnied.emvnfccard.model.EmvCard
import com.github.devnied.emvnfccard.parser.EmvTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NfcCreditCardReader {
    suspend fun readCard(intent: Intent): EmvCard? = withContext(Dispatchers.Default) {
        if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            val tag = intent.getParcelableExtraCompat<Tag>(NfcAdapter.EXTRA_TAG)
            val isoDep = IsoDep.get(tag) ?: return@withContext null
            runCatching {
                isoDep.use {
                    it.connect()
                    val provider = EmvCardProvider(it)
                    val config = EmvTemplate.Config().apply {
                        readAt = true
                        readTransactions = false
                    }
                    val parser = EmvTemplate.Builder()
                        .setConfig(config)
                        .setProvider(provider)
                        .build()
                    return@withContext parser.readEmvCard()
                }
            }
        }
        return@withContext null
    }
}
