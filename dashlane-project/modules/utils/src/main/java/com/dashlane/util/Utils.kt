package com.dashlane.util



inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (ignored: Exception) {
    null
}



inline fun tryAsSuccess(block: () -> Unit): Boolean = try {
    block(); true
} catch (ignored: Exception) {
    false
}



inline fun <T> nullUnless(condition: Boolean, block: () -> T): T? = if (condition) block() else null



inline fun <T> T?.runIfNull(closure: () -> Unit): T? =
    if (this != null) {
        this
    } else {
        closure()
        this
    }



val <T> T.exhaustive: T
    get() = this