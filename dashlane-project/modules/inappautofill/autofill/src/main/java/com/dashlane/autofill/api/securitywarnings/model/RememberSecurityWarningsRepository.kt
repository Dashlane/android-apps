package com.dashlane.autofill.api.securitywarnings.model



interface RememberSecurityWarningsRepository {
    fun add(securityWarning: RememberSecurityWarning): Boolean
    fun has(securityWarning: RememberSecurityWarning): Boolean
    fun hasSource(securityWarning: RememberSecurityWarning): Boolean
    fun clearAll()
}
