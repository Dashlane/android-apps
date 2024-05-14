package com.dashlane.passphrase.generator

import android.content.Context
import com.dashlane.passphrase.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

const val PASSPHRASE_LENGTH = 5

class PassphraseGenerator @Inject constructor(@ApplicationContext private val context: Context) {

    fun getWordList(): List<String> {
        val raw: InputStream = context.resources.openRawResource(R.raw.eff_large_wordlist)
        val wordList: MutableList<String> = mutableListOf()
        raw.bufferedReader().forEachLine { wordList += it.split("\t")[1] }
        return wordList
    }

    fun generatePassphrase(wordList: List<String>, wordSeed: ByteArray): List<String> {
        val wordListSize: UInt = wordList.size.toUInt()
        
        val maxRandom: UInt = UInt.MAX_VALUE / wordListSize * wordListSize
        val passphraseList: MutableList<String> = mutableListOf()

        for (i in 0..wordSeed.size step Int.SIZE_BYTES) {
            
            val current = wordSeed.getUIntAt(i)
            if (current < maxRandom) {
                passphraseList += wordList[(current % wordListSize).toInt()]
            }
            
            if (passphraseList.size == PASSPHRASE_LENGTH) break
        }

        return passphraseList
    }

    private fun ByteArray.getUIntAt(i: Int): UInt {
        return ByteBuffer.wrap(this, i, 4).order(ByteOrder.LITTLE_ENDIAN).int.toUInt()
    }
}
