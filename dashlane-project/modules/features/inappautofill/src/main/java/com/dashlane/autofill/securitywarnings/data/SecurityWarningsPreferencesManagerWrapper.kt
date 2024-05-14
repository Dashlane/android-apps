package com.dashlane.autofill.securitywarnings.data

interface SecurityWarningsPreferencesManagerWrapper {
    val incorrectJsonKey: String
    val unknownJsonKey: String

    fun remove(key: String): Boolean
    fun getString(key: String): String?
    fun putString(key: String, stringSet: String): Boolean
}
