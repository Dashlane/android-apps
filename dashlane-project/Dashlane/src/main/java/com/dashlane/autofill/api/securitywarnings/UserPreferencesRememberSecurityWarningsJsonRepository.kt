package com.dashlane.autofill.api.securitywarnings

import com.dashlane.autofill.securitywarnings.data.SecurityWarningsJson
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarning
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsRepository
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.google.gson.Gson
import javax.inject.Inject

class UserPreferencesRememberSecurityWarningsJsonRepository @Inject constructor(
    sessionManager: SessionManager,
    preferencesManager: PreferencesManager
) : RememberSecurityWarningsRepository {
    private val userPreferencesManager: UserPreferencesManager = preferencesManager[sessionManager.session?.username]

    private enum class PreferencesType {
        INCORRECT,
        UNKNOWN
    }

    private val gson = Gson()

    private val incorrectJsonKey: String = ConstantsPrefs.AUTOFILL_REMEMBER_SECURITY_WARNINGS_INCORRECT_JSON
    private val unknownJsonKey: String = ConstantsPrefs.AUTOFILL_REMEMBER_SECURITY_WARNINGS_UNKNOWN_JSON
    private val incorrectJson: SecurityWarningsJson
        get() = userPreferencesManager.getString(incorrectJsonKey).toJson() ?: SecurityWarningsJson(PreferencesType.INCORRECT.name)

    private val unknownJson
        get() = userPreferencesManager.getString(unknownJsonKey).toJson() ?: SecurityWarningsJson(PreferencesType.UNKNOWN.name)

    private fun String?.toJson(): SecurityWarningsJson? = gson.fromJson(this, SecurityWarningsJson::class.java)

    private fun SecurityWarningsJson.fromJson(): String = gson.toJson(this)

    override fun add(securityWarning: RememberSecurityWarning): Boolean {
        return when (securityWarning.signatureVerification) {
            is SignatureVerification.Incorrect -> add(incorrectJson, securityWarning)
            is SignatureVerification.UnknownWithSignature -> add(unknownJson, securityWarning)
            else -> false
        }
    }

    override fun has(securityWarning: RememberSecurityWarning): Boolean {
        return when (securityWarning.signatureVerification) {
            is SignatureVerification.Incorrect -> has(incorrectJson, securityWarning)
            is SignatureVerification.UnknownWithSignature -> has(unknownJson, securityWarning)
            else -> false
        }
    }

    override fun hasSource(securityWarning: RememberSecurityWarning): Boolean {
        return when (securityWarning.signatureVerification) {
            is SignatureVerification.Incorrect -> hasSource(incorrectJson, securityWarning)
            is SignatureVerification.UnknownWithSignature -> hasSource(unknownJson, securityWarning)
            else -> false
        }
    }

    override fun clearAll() {
        userPreferencesManager.remove(incorrectJsonKey)
        userPreferencesManager.remove(unknownJsonKey)
    }

    private fun add(securityWarningsJson: SecurityWarningsJson, securityWarning: RememberSecurityWarning): Boolean {
        val signatures = securityWarning.allSignatures() ?: return false

        val updatedItemsJson = signatures.fold(securityWarningsJson) { s, a ->
            s.add(a, securityWarning.item, securityWarning.source) ?: return false
        }
        val updatedSourcesJson = signatures.fold(updatedItemsJson) { s, a ->
            s.add(a, securityWarning.source) ?: return false
        }

        return syncInPreferences(securityWarningsJson, updatedSourcesJson)
    }

    private fun has(securityWarningsJson: SecurityWarningsJson, securityWarning: RememberSecurityWarning): Boolean {
        val signatures = securityWarning.allSignatures() ?: return false

        return signatures.any {
            securityWarningsJson.has(it, securityWarning.item, securityWarning.source)
        }
    }

    private fun hasSource(
        securityWarningsJson: SecurityWarningsJson,
        securityWarning: RememberSecurityWarning
    ): Boolean {
        val signatures = securityWarning.allSignatures() ?: return false

        return signatures.any {
            securityWarningsJson.has(it, securityWarning.source)
        }
    }

    private fun syncInPreferences(existingData: SecurityWarningsJson, newData: SecurityWarningsJson): Boolean {
        if (existingData != newData) {
            val preferenceType = enumValues<PreferencesType>().firstOrNull { it.name == newData.securityWarningType } ?: return false

            val preferencesKey = when (preferenceType) {
                PreferencesType.INCORRECT -> incorrectJsonKey
                PreferencesType.UNKNOWN -> unknownJsonKey
            }
            val jsonString = newData.fromJson()

            return userPreferencesManager.putString(preferencesKey, jsonString)
        }
        return true
    }
}
