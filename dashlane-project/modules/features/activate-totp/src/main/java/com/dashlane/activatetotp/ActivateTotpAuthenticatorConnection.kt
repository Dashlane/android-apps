package com.dashlane.activatetotp

import kotlinx.coroutines.Deferred

interface ActivateTotpAuthenticatorConnection {
    fun hasSaveDashlaneTokenAsync(): Deferred<Boolean>
    fun saveDashlaneTokenAsync(userId: String, otpUri: String): Deferred<Boolean>
    fun deleteDashlaneTokenAsync(userId: String): Deferred<Boolean>
}