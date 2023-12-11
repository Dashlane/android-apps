package com.dashlane.login.pages.totp

import com.dashlane.account.UserAccountStorageImpl
import com.dashlane.account.UserSecuritySettings
import com.dashlane.authentication.AuthenticationAccountConfigurationChangedException
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationLockedOutException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationSecondFactorFailedException
import com.dashlane.authentication.AuthenticationTimeoutException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.core.u2f.U2fChallenge
import com.dashlane.core.u2f.U2fKey
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.LoginBaseDataProvider
import com.dashlane.login.pages.totp.u2f.U2fKeyDetector
import com.dashlane.util.NetworkStateProvider
import com.dashlane.util.tryOrNull
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class LoginTotpDataProvider @Inject constructor(
    private val network: NetworkStateProvider,
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val u2fKeyDetector: U2fKeyDetector,
    private val loggerFactory: LoginTotpLogger.Factory,
    private val userAccountStorage: UserAccountStorageImpl
) : LoginBaseDataProvider<LoginTotpContract.Presenter>(), LoginTotpContract.DataProvider {

    override lateinit var secondFactor: AuthenticationSecondFactor.Totp

    private val logger by lazy {
        loggerFactory.create(
            accountExists,
            secondFactor.toSecuritySettings(otp2 = accountExists)
        )
    }

    private val accountExists
        get() = userAccountStorage[secondFactor.login] != null

    override val username: String
        get() = secondFactor.login

    private var u2fInProgress = false

    override suspend fun validateTotp(otp: String, auto: Boolean, isRecoveryCode: Boolean) {
        try {
            val result = secondFactoryRepository.validate(secondFactor, otp)
            handleTotpSuccess(
                result.registeredUserDevice,
                result.authTicket
            )
        } catch (e: AuthenticationException) {
            when (e) {
                is AuthenticationSecondFactorFailedException,
                is AuthenticationAccountConfigurationChangedException -> handleTotpError(
                    auto = auto,
                    lockedOut = false,
                    isRecoveryCode = isRecoveryCode
                )
                is AuthenticationLockedOutException -> handleTotpError(auto, lockedOut = true, isRecoveryCode = isRecoveryCode)
                is AuthenticationOfflineException -> {
                    throw LoginBaseContract.OfflineException(e)
                }
                else -> handleTotpNetworkError()
            }
        }
    }

    override suspend fun executeDuoAuthentication() {
        val duoPush = AuthenticationSecondFactor.DuoPush(
            login = username,
            securityFeatures = secondFactor.securityFeatures
        )
        try {
            val result = secondFactoryRepository.validate(duoPush)
            handleTotpSuccess(
                result.registeredUserDevice,
                result.authTicket
            )
        } catch (e: AuthenticationException) {
            when (e) {
                is AuthenticationSecondFactorFailedException,
                    
                is AuthenticationAccountConfigurationChangedException -> {
                    throw LoginTotpContract.DuoDeniedException(e)
                }
                is AuthenticationTimeoutException -> {
                    throw LoginTotpContract.DuoTimeoutException(e)
                }
                is AuthenticationOfflineException -> {
                    throw LoginBaseContract.OfflineException(e)
                }
                is AuthenticationExpiredVersionException -> {
                    throw LoginBaseContract.ExpiredVersionException(e)
                }
                else -> {
                    throw LoginBaseContract.NetworkException(e)
                }
            }
        }
    }

    override suspend fun executeU2fAuthentication(coroutineScope: CoroutineScope) {
        if (u2fInProgress) return
        u2fInProgress = true

        checkOnline { }

        val u2fSecondFactor = secondFactor.u2f ?: return
        val u2fChallenges =
            u2fSecondFactor.challenges.map { it.toU2fChallenge() }
        if (u2fChallenges.isEmpty()) {
            presenter.notifyUnknownError()
            return
        }
        val signedChallenge = signU2fChallenge(coroutineScope, u2fChallenges) ?: return
        validateU2f(u2fSecondFactor, signedChallenge)
    }

    private suspend fun validateU2f(
        u2fSecondFactor: AuthenticationSecondFactor.U2f,
        challengeAnswer: U2fKey.SignedChallenge
    ) {
        try {
            val result = secondFactoryRepository.validate(
                u2fSecondFactor,
                challengeAnswer.challenge,
                challengeAnswer.signatureData,
                challengeAnswer.clientData
            )
            handleTotpSuccess(
                result.registeredUserDevice,
                result.authTicket
            )
        } catch (e: AuthenticationException) {
            when (e) {
                is AuthenticationSecondFactorFailedException,
                is AuthenticationAccountConfigurationChangedException -> handleTotpError(
                    auto = false,
                    lockedOut = false,
                    isRecoveryCode = false
                )
                is AuthenticationOfflineException -> {
                    presenter.notifyOffline()
                }
                is AuthenticationExpiredVersionException -> {
                    presenter.notifyExpiredVersion()
                }
                else -> {
                    presenter.notifyU2fKeyMatchFailError()
                }
            }
        }
    }

    private suspend fun signU2fChallenge(
        coroutineScope: CoroutineScope,
        challenges: List<U2fChallenge>
    ): U2fKey.SignedChallenge? {
        
        val u2fKey = u2fKeyDetector.detectKey(coroutineScope)
        presenter.notifyU2fKeyDetected()
        val challengeAnswer = tryOrNull {
            if (u2fKey.requireUserAction) presenter.notifyU2fKeyNeedsUserPresence()
            u2fKey.signChallenges(challenges)
        }
        if (challengeAnswer == null) {
            
            u2fKeyDetector.ignore(u2fKey)
            u2fInProgress = false
            presenter.notifyU2fKeyMatchFailError()
        } else {
            presenter.notifyU2fKeyMatched()
        }
        return challengeAnswer
    }

    private fun handleTotpError(auto: Boolean, lockedOut: Boolean, isRecoveryCode: Boolean) {
        logger.logInvalidTotp(auto)
        presenter.notifyTotpError(lockedOut, isRecoveryCode)
    }

    private fun handleTotpNetworkError() {
        presenter.notifyNetworkError()
    }

    private fun handleTotpSuccess(
        registeredUserDevice: RegisteredUserDevice,
        authTicket: String?
    ) {
        u2fKeyDetector.cancel()
        presenter.onTotpSuccess(registeredUserDevice, authTicket)
        u2fInProgress = false
    }

    private inline fun checkOnline(block: () -> Unit) {
        val offline = !network.isOn()
        if (offline) {
            block()
            throw LoginBaseContract.OfflineException()
        }
    }
}

private fun AuthenticationSecondFactor.Totp.toSecuritySettings(otp2: Boolean) =
    UserSecuritySettings(
        isToken = false,
        isTotp = true,
        isOtp2 = otp2,
        isDuoEnabled = isDuoPushEnabled,
        isU2fEnabled = isU2fEnabled
    )

private fun AuthenticationSecondFactor.U2f.Challenge.toU2fChallenge() = U2fChallenge(
    version = version,
    origin = appId,
    keyHandle = keyHandle,
    challenge = challenge
)
