package com.dashlane.autofill.util

fun interface IdGenerator {
    fun nextId(): String
}