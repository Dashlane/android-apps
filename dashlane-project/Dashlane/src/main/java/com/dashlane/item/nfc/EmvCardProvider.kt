package com.dashlane.item.nfc

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.parser.IProvider



class EmvCardProvider(private val tagCom: IsoDep) : IProvider {

    override fun transceive(command: ByteArray): ByteArray? =
        runCatching { tagCom.transceive(command) }.getOrNull()

    override fun getAt() = if (tagCom.isConnected) {
        
        tagCom.historicalBytes ?: tagCom.hiLayerResponse ?: ByteArray(0)
    } else {
        ByteArray(0)
    }
}
