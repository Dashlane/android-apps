package com.dashlane.createaccount.pages.confirmpassword

import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateAccountConfirmPasswordDataProvider @Inject constructor(
    private val biometricAuthModule: BiometricAuthModule,
) : BaseDataProvider<CreateAccountConfirmPasswordContract.Presenter>(),
    CreateAccountConfirmPasswordContract.DataProvider {

    override val requiresTosApproval by lazy {
        return@lazy inEuropeanUnion
    }

    override val biometricAvailable: Boolean
        get() = biometricAuthModule.isHardwareSetUp() && !biometricAuthModule.isOnlyWeakSupported()

    override val clearPassword: String
        get() = masterPassword.decodeUtf8ToString()

    lateinit var username: String
    lateinit var masterPassword: ObfuscatedByteArray
    var inEuropeanUnion: Boolean = true

    override suspend fun validatePassword(password: CharSequence) =
        withContext(Dispatchers.Default) {
            val matches = password.encodeUtf8ToObfuscated().use { masterPassword == it }
            if (matches) {
                CreateAccountConfirmPasswordContract.PasswordSuccess(username, masterPassword)
            } else {
                throw CreateAccountConfirmPasswordContract.PasswordMismatchException()
            }
        }
}
