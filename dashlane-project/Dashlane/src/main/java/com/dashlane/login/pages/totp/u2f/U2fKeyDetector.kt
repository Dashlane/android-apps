package com.dashlane.login.pages.totp.u2f

import com.dashlane.core.u2f.U2fKey
import kotlinx.coroutines.CoroutineScope

interface U2fKeyDetector {
    

    suspend fun detectKey(coroutineScope: CoroutineScope): U2fKey

    

    fun cancel()

    

    fun ignore(u2fKey: U2fKey)
}