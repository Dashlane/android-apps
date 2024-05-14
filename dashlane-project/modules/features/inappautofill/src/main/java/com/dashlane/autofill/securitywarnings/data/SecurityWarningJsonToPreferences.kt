package com.dashlane.autofill.securitywarnings.data

internal interface SecurityWarningJsonToPreferences {
    val incorrectJson: SecurityWarningsJson
    val unknownJson: SecurityWarningsJson

    fun syncInPreferences(existingData: SecurityWarningsJson, newData: SecurityWarningsJson): Boolean
    fun clearAll()
}
