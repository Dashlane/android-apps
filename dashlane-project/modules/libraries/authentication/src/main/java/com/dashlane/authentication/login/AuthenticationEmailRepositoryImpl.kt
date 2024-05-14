package com.dashlane.authentication.login

import com.dashlane.authentication.AuthenticationAccountNotFoundException
import com.dashlane.authentication.AuthenticationContactSsoAdministratorException
import com.dashlane.authentication.AuthenticationEmptyEmailException
import com.dashlane.authentication.AuthenticationInvalidEmailException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.AuthenticationTeamException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.UnauthenticatedUser
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresDeviceRegistration
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresPassword
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresServerKey
import com.dashlane.authentication.login.AuthenticationEmailRepository.Result.RequiresSso
import com.dashlane.authentication.toAuthenticationException
import com.dashlane.authentication.toRegisteredUserDevice
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.endpoints.AccountType
import com.dashlane.server.api.endpoints.authentication.Auth2faUnauthenticatedSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthLoginService
import com.dashlane.server.api.endpoints.authentication.AuthMethod
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationEmailService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.endpoints.authentication.AuthSendEmailTokenService
import com.dashlane.server.api.endpoints.authentication.AuthU2fChallenge
import com.dashlane.server.api.endpoints.authentication.AuthVerification
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceDeactivatedException
import com.dashlane.server.api.endpoints.authentication.exceptions.DeviceNotFoundException
import com.dashlane.server.api.endpoints.authentication.exceptions.SsoBlockedException
import com.dashlane.server.api.endpoints.authentication.exceptions.TeamGenericErrorException
import com.dashlane.server.api.endpoints.authentication.exceptions.UserNotFoundException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException
import com.dashlane.util.isValidEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthenticationEmailRepositoryImpl(
    private val userStorage: UserStorage,
    private val emailService: AuthRegistrationEmailService,
    private val loginService: AuthLoginService,
    private val sendEmailTokenService: AuthSendEmailTokenService,
    private val unauthenticated2faSettingsService: Auth2faUnauthenticatedSettingsService,
    private val connectivityCheck: ConnectivityCheck,
    private val dashlaneTime: DashlaneTime
) : AuthenticationEmailRepository {

    override suspend fun getUserStatus(unauthenticatedUser: UnauthenticatedUser): AuthenticationEmailRepository.Result =
        withContext(Dispatchers.Default) { getUserStatusImpl(unauthenticatedUser) }

    private suspend fun getUserStatusImpl(unauthenticatedUser: UnauthenticatedUser): AuthenticationEmailRepository.Result {
        val login = unauthenticatedUser.email.trim().lowercase().also {
            if (it.isEmpty()) {
                throw AuthenticationEmptyEmailException()
            }
            if (!it.isValidEmail()) {
                throw AuthenticationInvalidEmailException()
            }
        }

        val userDevice = userStorage.getUser(login)
        return if (userDevice == null) {
            
            if (connectivityCheck.isOnline) {
                fetchOrRestoreUserStatus(login, unauthenticatedUser)
            } else {
                throw AuthenticationOfflineException()
            }
        } else {
            
            
            
            
            
            
            
            if (connectivityCheck.isOnline) {
                getRemoteDeviceStatus(userDevice)
            } else {
                if (userDevice.isServerKeyNeeded || userDevice.sso) {
                    
                    throw AuthenticationOfflineException()
                } else {
                    
                    userDevice.toRequiresPassword(ssoInfo = null)
                }
            }
        }
    }

    private suspend fun fetchOrRestoreUserStatus(
        login: String,
        unauthenticatedUser: UnauthenticatedUser
    ): AuthenticationEmailRepository.Result {
        val cipheredBackupToken = unauthenticatedUser.cipheredBackupToken
        val time = dashlaneTime.getClockOrDefault().instant()
        return if (cipheredBackupToken != null && cipheredBackupToken.isValid(time)) {
            getRemoteUserStatusForRestore(login, cipheredBackupToken.token)
        } else {
            getRemoteUserStatus(login)
        }
    }

    private suspend fun getRemoteUserStatusForRestore(
        login: String,
        cipheredBackupToken: String
    ): AuthenticationEmailRepository.Result {
        val request = Auth2faUnauthenticatedSettingsService.Request(
            login = login
        )
        val response = try {
            unauthenticated2faSettingsService.execute(request)
        } catch (e: UserNotFoundException) {
            throw AuthenticationAccountNotFoundException(cause = e)
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException(endpoint = AuthenticationNetworkException.Endpoint.REGISTRATION)
        }

        val responseData = response.data
        return if (responseData.type == AuthSecurityType.EMAIL_TOKEN && responseData.ssoInfo?.migration == null) {
            
            
            
            RequiresPassword(
                RegisteredUserDevice.ToRestore(
                    login,
                    setOf(SecurityFeature.EMAIL_TOKEN),
                    cipheredBackupToken
                )
            )
        } else {
            
            getRemoteUserStatus(login)
        }
    }

    private suspend fun getRemoteUserStatus(login: String): AuthenticationEmailRepository.Result {
        val request = AuthRegistrationEmailService.Request(
            login = login,
            methods = listOf(
                AuthMethod.EMAIL_TOKEN,
                AuthMethod.TOTP,
                AuthMethod.DUO_PUSH,
                AuthMethod.U2F,
                AuthMethod.DASHLANE_AUTHENTICATOR
            )
        )
        val response = try {
            emailService.execute(request)
        } catch (e: UserNotFoundException) {
            throw AuthenticationAccountNotFoundException(cause = e)
        } catch (e: SsoBlockedException) {
            throw AuthenticationContactSsoAdministratorException(cause = e)
        } catch (e: TeamGenericErrorException) {
            throw AuthenticationTeamException(cause = e)
        } catch (e: DashlaneApiException) {
            throw e.toAuthenticationException(endpoint = AuthenticationNetworkException.Endpoint.REGISTRATION)
        }

        val responseData = response.data
        val verification = responseData.verifications.associateBy { it.type }
        val ssoInfo = verification[AuthVerification.Type.SSO]?.ssoInfo?.toAuthenticationSsoInfo()

        val secondFactor = when {
            AuthVerification.Type.EMAIL_TOKEN in verification -> AuthenticationSecondFactorEmailToken(
                login,
                verification = verification
            ).also {
                try {
                    if (responseData.accountType == AccountType.MASTERPASSWORD) {
                        
                        sendEmailTokenService.execute(AuthSendEmailTokenService.Request(login = login))
                    }
                } catch (e: DashlaneApiException) {
                    throw e.toAuthenticationException()
                }
            }
            AuthVerification.Type.TOTP in verification -> AuthenticationSecondFactorTotp(
                login,
                verification = verification
            )
            AuthVerification.Type.DASHLANE_AUTHENTICATOR in verification -> AuthenticationSecondFactorEmailToken(
                login,
                verification = verification
            )
            AuthVerification.Type.SSO in verification -> return RequiresDeviceRegistration.Sso(
                login,
                ssoInfo ?: throw AuthenticationUnknownException(message = "Missing ssoInfo")
            )
            else -> throw AuthenticationUnknownException(message = "Unknown verification")
        }

        return RequiresDeviceRegistration.SecondFactor(secondFactor = secondFactor, accountType = responseData.accountType, ssoInfo = ssoInfo)
    }

    private suspend fun getRemoteDeviceStatus(userDevice: UserStorage.UserDevice): AuthenticationEmailRepository.Result {
        val response = try {
            loginService.execute(userDevice.toLoginServiceRequest())
        } catch (e: DeviceDeactivatedException) {
            return resetUser(userDevice.login, "Device deactivated ${userDevice.login}")
        } catch (e: DeviceNotFoundException) {
            return resetUser(userDevice.login, "Access key invalid ${userDevice.login}")
        } catch (e: DashlaneApiHttp400BusinessException) {
            throw AuthenticationUnknownException(cause = e)
        } catch (e: DashlaneApiException) {
            return userDevice.toLocalResult(e)
        }

        val responseData = response.data

        responseData.profilesToDelete.orEmpty().forEach {
            userStorage.clearUser(it.login, "Profile deleted ${it.login}")
        }

        val verification = responseData.verifications.associateBy { it.type }

        val ssoInfo = verification[AuthVerification.Type.SSO]?.ssoInfo?.toAuthenticationSsoInfo()

        return when {
            verification.isEmpty() -> {
                when (responseData.accountType) {
                    AccountType.MASTERPASSWORD -> userDevice.toRequiresPassword(ssoInfo)
                    AccountType.INVISIBLEMASTERPASSWORD -> RequiresDeviceRegistration.SecondFactor(
                        secondFactor = AuthenticationSecondFactorEmailToken(
                            login = userDevice.login,
                            verification = verification
                        ),
                        accountType = responseData.accountType,
                        ssoInfo = null
                    )
                }
            }
            AuthVerification.Type.DASHLANE_AUTHENTICATOR in verification ||
                AuthVerification.Type.TOTP in verification -> RequiresServerKey(
                AuthenticationSecondFactorTotp(userDevice.login, verification),
                ssoInfo
            )
            AuthVerification.Type.SSO in verification -> RequiresSso(
                userDevice.login,
                ssoInfo ?: throw AuthenticationUnknownException(message = "Missing ssoInfo")
            )
            else -> throw AuthenticationUnknownException(message = "Unknown verification")
        }
    }

    private suspend fun resetUser(
        login: String,
        reason: String
    ): AuthenticationEmailRepository.Result {
        userStorage.clearUser(login, reason)
        return getRemoteUserStatus(login)
    }
}

private fun UserStorage.UserDevice.toLocalResult(e: DashlaneApiException): AuthenticationEmailRepository.Result =
    if (isServerKeyNeeded || sso) {
        throw e.toAuthenticationException(endpoint = AuthenticationNetworkException.Endpoint.LOGIN)
    } else {
        toRequiresPassword(ssoInfo = null)
    }

private fun UserStorage.UserDevice.toRequiresPassword(ssoInfo: SsoInfo?): RequiresPassword =
    RequiresPassword(toRegisteredUserDevice(), ssoInfo)

private fun List<AuthU2fChallenge>.toU2fChallenges(): List<AuthenticationSecondFactor.U2f.Challenge> =
    map {
        AuthenticationSecondFactor.U2f.Challenge(
            appId = it.appId,
            challenge = it.challenge,
            version = it.version,
            keyHandle = it.keyHandle
        )
    }

private fun UserStorage.UserDevice.toLoginServiceRequest() = AuthLoginService.Request(
    login = login,
    deviceAccessKey = accessKey,
    profiles = listOf(
        AuthLoginService.Request.Profile(
            login = login,
            deviceAccessKey = accessKey
        )
    ),
    methods = listOf(
        AuthMethod.TOTP,
        AuthMethod.DUO_PUSH,
        AuthMethod.U2F,
        AuthMethod.DASHLANE_AUTHENTICATOR
    )
)

@Suppress("FunctionName")
private fun AuthenticationSecondFactorTotp(
    login: String,
    verification: Map<AuthVerification.Type, AuthVerification>
): AuthenticationSecondFactor.Totp {
    val isDuoPushEnabled = verification[AuthVerification.Type.DUO_PUSH] != null
    val u2f = verification[AuthVerification.Type.U2F]
    val isU2fEnabled = u2f != null
    val u2fChallenges = u2f?.u2fChallenges
    val isAuthenticatorEnabled = verification[AuthVerification.Type.DASHLANE_AUTHENTICATOR] != null

    val securityFeatures = sequence {
        yield(SecurityFeature.TOTP)
        if (isDuoPushEnabled) yield(SecurityFeature.DUO)
        if (isU2fEnabled) yield(SecurityFeature.U2F)
        if (isAuthenticatorEnabled) yield(SecurityFeature.AUTHENTICATOR)
    }.toSet()

    return AuthenticationSecondFactor.Totp(
        login = login,
        securityFeatures = securityFeatures,
        duoPush = if (isDuoPushEnabled) {
            AuthenticationSecondFactor.DuoPush(
                login = login,
                securityFeatures = securityFeatures
            )
        } else {
            null
        },
        u2f = if (isU2fEnabled) {
            u2fChallenges?.takeUnless { it.isEmpty() }?.toU2fChallenges()?.let {
                AuthenticationSecondFactor.U2f(
                    login = login,
                    securityFeatures = securityFeatures,
                    challenges = it
                )
            }
        } else {
            null
        },
        authenticator = if (isAuthenticatorEnabled) {
            AuthenticationSecondFactor.Authenticator(
                login = login,
                securityFeatures = securityFeatures
            )
        } else {
            null
        }
    )
}

@Suppress("FunctionName")
private fun AuthenticationSecondFactorEmailToken(
    login: String,
    verification: Map<AuthVerification.Type, AuthVerification>
): AuthenticationSecondFactor.EmailToken {
    val isAuthenticatorEnabled = verification[AuthVerification.Type.DASHLANE_AUTHENTICATOR] != null

    return AuthenticationSecondFactor.EmailToken(
        login = login,
        authenticator = if (isAuthenticatorEnabled) {
            AuthenticationSecondFactor.Authenticator(
                login = login,
                securityFeatures = setOf(
                    SecurityFeature.EMAIL_TOKEN,
                    SecurityFeature.AUTHENTICATOR
                )
            )
        } else {
            null
        }
    )
}