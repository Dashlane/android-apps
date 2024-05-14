package com.dashlane.util

import androidx.annotation.Keep

fun Throwable.anonymize(): Throwable {
    
    if (this is SecureException) return this

    val obfuscatedThrowable = AnonymousException(javaClass.name, cause?.anonymize())
    obfuscatedThrowable.stackTrace = stackTrace
    return obfuscatedThrowable
}

@Keep
open class SecureException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class AnonymousException(message: String, cause: Throwable?) : Throwable(message, cause)