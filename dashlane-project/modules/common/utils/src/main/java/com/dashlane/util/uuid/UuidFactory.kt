package com.dashlane.util.uuid

fun interface UuidFactory {
    fun generateUuid(): String
}