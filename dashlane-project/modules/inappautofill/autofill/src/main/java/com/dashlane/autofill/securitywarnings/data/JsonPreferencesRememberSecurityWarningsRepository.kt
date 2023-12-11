package com.dashlane.autofill.securitywarnings.data

import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarning
import com.dashlane.autofill.securitywarnings.model.RememberSecurityWarningsRepository
import com.dashlane.core.helpers.SignatureVerification

class JsonPreferencesRememberSecurityWarningsRepository constructor(
    preferencesManager: SecurityWarningsPreferencesManagerWrapper
) : RememberSecurityWarningsRepository {
    private val securityWarningJsonSync: SecurityWarningJsonToPreferences =
        SecurityWarningJsonToPreferencesNoCache(preferencesManager)
    private val incorrectJson
        get() = securityWarningJsonSync.incorrectJson
    private val unknownJson
        get() = securityWarningJsonSync.unknownJson

    override fun add(securityWarning: RememberSecurityWarning): Boolean {
        return when (securityWarning.signatureVerification) {
            is SignatureVerification.Incorrect -> add(incorrectJson, securityWarning)
            is SignatureVerification.UnknownWithSignature -> add(unknownJson, securityWarning)
            else -> false
        }
    }

    private fun add(securityWarningsJson: SecurityWarningsJson, securityWarning: RememberSecurityWarning): Boolean {
        val signatures = securityWarning.allSignatures() ?: return false

        val updatedItemsJson = signatures.fold(securityWarningsJson) { s, a ->
            s.add(a, securityWarning.item, securityWarning.source) ?: return false
        }
        val updatedSourcesJson = signatures.fold(updatedItemsJson) { s, a ->
            s.add(a, securityWarning.source) ?: return false
        }

        return securityWarningJsonSync.syncInPreferences(securityWarningsJson, updatedSourcesJson)
    }

    override fun has(securityWarning: RememberSecurityWarning): Boolean {
        return when (securityWarning.signatureVerification) {
            is SignatureVerification.Incorrect -> has(incorrectJson, securityWarning)
            is SignatureVerification.UnknownWithSignature -> has(unknownJson, securityWarning)
            else -> false
        }
    }

    private fun has(securityWarningsJson: SecurityWarningsJson, securityWarning: RememberSecurityWarning): Boolean {
        val signatures = securityWarning.allSignatures() ?: return false

        return signatures.any {
            securityWarningsJson.has(it, securityWarning.item, securityWarning.source)
        }
    }

    override fun hasSource(securityWarning: RememberSecurityWarning): Boolean {
        return when (securityWarning.signatureVerification) {
            is SignatureVerification.Incorrect -> hasSource(incorrectJson, securityWarning)
            is SignatureVerification.UnknownWithSignature -> hasSource(unknownJson, securityWarning)
            else -> false
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

    override fun clearAll() {
        securityWarningJsonSync.clearAll()
    }
}
