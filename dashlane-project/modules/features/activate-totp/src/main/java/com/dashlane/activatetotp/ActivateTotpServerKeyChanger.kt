package com.dashlane.activatetotp



interface ActivateTotpServerKeyChanger {
    suspend fun updateServerKey(
        newServerKey: String?,
        authTicket: String
    ): Boolean
}