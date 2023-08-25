package com.dashlane.core.u2f.transport

import com.dashlane.core.u2f.U2fChallenge
import java.io.Closeable

interface Transport : Closeable {
    suspend fun init(): Boolean

    fun sign(challenge: U2fChallenge): ByteArray?
}