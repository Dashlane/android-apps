package com.dashlane.item.nfc

import android.annotation.SuppressLint
import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.github.devnied.emvnfccard.model.EmvCard
import com.github.devnied.emvnfccard.parser.EmvTemplate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcCreditCardReader @Inject constructor(
    @DefaultCoroutineDispatcher
    private val dispatcher: CoroutineDispatcher,
) {
    @SuppressLint("JavaUtilDateUsage")
    suspend fun readTag(tag: Tag): EmvCard? = withContext(dispatcher) {
        val isoDep = IsoDep.get(tag) ?: return@withContext null
        try {
            isoDep.use {
                it.connect()
                val provider = EmvCardProvider(it)
                val config = EmvTemplate.GetConfig().apply {
                    readAt = true
                    readTransactions = false
                }
                val parser = EmvTemplate.GetBuilder()
                    .setConfig(config)
                    .setProvider(provider)
                    .build()
                return@withContext parser.readEmvCard(Calendar.getInstance())
            }
        } catch (e: Exception) {
        }
        return@withContext null
    }
}