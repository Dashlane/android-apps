package com.dashlane.createaccount.pages.confirmpassword

import com.dashlane.createaccount.AccountCreator
import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.logD
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateAccountConfirmPasswordDataProvider @Inject constructor(
    private val biometricAuthModule: BiometricAuthModule,
    private val accountCreator: AccountCreator,
    private val logger: CreateAccountConfirmPasswordLogger,
    private val installLogRepository: InstallLogRepository
) : BaseDataProvider<CreateAccountConfirmPasswordContract.Presenter>(),
    CreateAccountConfirmPasswordContract.DataProvider {

    override val requiresTosApproval by lazy {
        return@lazy if (accountCreator.isGdprDebugModeEnabled) {
            accountCreator.isGdprForced
        } else {
            inEuropeanUnion
        }
    }

    override val biometricAvailable: Boolean
        get() = biometricAuthModule.isHardwareSetUp()

    override val clearPassword: String
        get() = masterPassword.decodeUtf8ToString()

    lateinit var username: String
    lateinit var masterPassword: ObfuscatedByteArray
    var inEuropeanUnion: Boolean = true
    var country: String? = null
    var origin: String? = null
        set(value) {
            field = value
            logger.origin = value
        }

    override fun onShow() {
        logger.logLand(requiresTosApproval)
    }

    override fun onBack() = logger.logBack()

    override fun passwordVisibilityToggled(passwordShown: Boolean) = logger.logPasswordVisibilityToggle(passwordShown)

    override suspend fun validatePassword(password: CharSequence) =
        withContext(Dispatchers.Default) {
            val matches = password.encodeUtf8ToObfuscated().use { masterPassword == it }
            logD { "Passwords match: $matches" }
            if (matches) {
                installLogRepository.enqueue(InstallLogCode17(subStep = "30"))
                CreateAccountConfirmPasswordContract.PasswordSuccess(username, masterPassword)
            } else {
                logger.logPasswordError()
                installLogRepository.enqueue(InstallLogCode17(subStep = "29"))
                throw CreateAccountConfirmPasswordContract.PasswordMismatchException()
            }
        }
}
