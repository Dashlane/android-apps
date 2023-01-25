package com.dashlane.autofill.api.securitywarnings.data

import com.google.gson.Gson



internal class SecurityWarningJsonToPreferencesNoCache constructor(
    private val preferencesManager: SecurityWarningsPreferencesManagerWrapper
) : SecurityWarningJsonToPreferences {

    private enum class PreferencesType {
        INCORRECT,
        UNKNOWN
    }

    private val gson = Gson()

    override val incorrectJson
        get() = preferencesManager.getString(preferencesManager.incorrectJsonKey).toJson()
            ?: SecurityWarningsJson(PreferencesType.INCORRECT.name)

    override val unknownJson
        get() = preferencesManager.getString(preferencesManager.unknownJsonKey).toJson()
            ?: SecurityWarningsJson(PreferencesType.UNKNOWN.name)

    private fun String?.toJson(): SecurityWarningsJson? = gson.fromJson(this, SecurityWarningsJson::class.java)

    private fun SecurityWarningsJson.fromJson(): String = gson.toJson(this)

    override fun syncInPreferences(existingData: SecurityWarningsJson, newData: SecurityWarningsJson): Boolean {
        if (existingData != newData) {
            val preferencesKey = newData.getPreferenceKey() ?: return false
            val jsonString = newData.fromJson()

            return preferencesManager.putString(preferencesKey, jsonString)
        }
        return true
    }

    private fun SecurityWarningsJson.getPreferenceKey(): String? {
        val preferenceType = enumValues<PreferencesType>().firstOrNull {
            it.name == this.securityWarningType
        } ?: return null

        return when (preferenceType) {
            PreferencesType.INCORRECT -> preferencesManager.incorrectJsonKey
            PreferencesType.UNKNOWN -> preferencesManager.unknownJsonKey
        }
    }

    override fun clearAll() {
        preferencesManager.remove(preferencesManager.incorrectJsonKey)
        preferencesManager.remove(preferencesManager.unknownJsonKey)
    }
}
