package com.dashlane.util

import java.time.Instant

fun Instant.isSemanticallyNull(): Boolean {
    return this == Instant.EPOCH
}