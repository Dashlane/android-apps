package com.dashlane.createaccount.passwordless

import com.dashlane.cryptography.ObfuscatedByteArray
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@ActivityRetainedScoped
class MplessAccountCreationRepository @Inject constructor() : Mutex by Mutex() {
    private var userData = UserData(
        email = "",
        pinCode = null,
        useBiometrics = false,
        password = null,
        termsOfServicesAccepted = false,
        offersAccepted = false,
    )

    suspend fun getUserData() = withLock { userData }

    suspend fun updateEmail(email: String) = withLock { userData = userData.copy(email = email) }

    suspend fun updatePassword(password: ObfuscatedByteArray) = withLock { userData = userData.copy(password = password) }

    suspend fun updatePinCode(pinCode: String) = withLock { userData = userData.copy(pinCode = pinCode) }

    suspend fun updateEnableBiometric(useBiometrics: Boolean) = withLock { userData = userData.copy(useBiometrics = useBiometrics) }

    suspend fun updateTermsOfServicesAccepted(termsOfServicesAccepted: Boolean) =
        withLock { userData = userData.copy(termsOfServicesAccepted = termsOfServicesAccepted) }

    suspend fun updateOffersAccepted(offersAccepted: Boolean) = withLock { userData = userData.copy(offersAccepted = offersAccepted) }
}