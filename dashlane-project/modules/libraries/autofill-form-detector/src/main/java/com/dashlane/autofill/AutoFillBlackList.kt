package com.dashlane.autofill

fun interface AutoFillBlackList {
    fun isBlackList(packageName: String): Boolean
}