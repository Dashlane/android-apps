package com.dashlane.login.pages.secrettransfer

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserSecuritySettings
import com.dashlane.authentication.AuthenticationInvalidPasswordException
import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.AuthenticationInvalidTokenException
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.UnauthenticatedUser
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationAuthTicketHelper
import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.authentication.login.findByTypeOrNull
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.sso.LoginSsoContract
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationAuthTicketService
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.server.api.endpoints.mpless.MplessCryptography
import com.dashlane.server.api.endpoints.mpless.MplessRequestTransferService
import com.dashlane.server.api.endpoints.mpless.MplessStartTransferService
import com.dashlane.session.AppKey
import com.dashlane.session.LocalKey
import com.dashlane.session.SessionInitializer
import com.dashlane.session.SessionResult
import com.dashlane.session.Username
import com.dashlane.session.VaultKey
import com.dashlane.util.extension.takeUntil
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.xml.domain.SyncObject
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

private const val SESSION_ERROR_MESSAGE = "Error on Session creation"

@OptIn(FlowPreview::class)
@HiltViewModel


@Suppress("LargeClass", "kotlin:S6313")
class LoginSecretTransferViewModel @Inject constructor(
    private val userAccountStorage: UserAccountStorage,
    private val userPreferencesManager: UserPreferencesManager,
    private val lockManager: LockManager,
    private val moshi: Moshi,
    private val sessionInitializer: SessionInitializer,
    private val jniCryptography: JniCryptography,
    private val mplessRequestTransferService: MplessRequestTransferService,
    private val mplessStartTransferService: MplessStartTransferService,
    private val authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    private val authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
    private val authenticationEmailRepository: AuthenticationEmailRepository,
    private val secondFactoryRepository: AuthenticationSecondFactoryRepository,
    private val authenticationAuthTicketHelper: AuthenticationAuthTicketHelper,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<LoginSecretTransferState>(LoginSecretTransferState.LoadingQR(LoginSecretTransferData()))
    private val eventSharedFlow = MutableSharedFlow<LoginSecretTransferEvent>()

    val uiState = stateFlow.asStateFlow()

    companion object {
        
        
        const val SALT = "AXbCCLBYulWaVNWT/YfT+SiuhBOlFqLFaPPI5/8XIio="
    }

    fun viewStarted() {
        if (stateFlow.value.data.qrCodeUri == null) generateQrCode()
    }

    fun emailConfirmed() = viewModelScope.launch { eventSharedFlow.emit(LoginSecretTransferEvent.EmailConfirmed) }

    fun cancel() {
        viewModelScope.launch { eventSharedFlow.emit(LoginSecretTransferEvent.Cancel) }
    }

    fun totpCompleted(otp: String) {
        viewModelScope.launch { eventSharedFlow.emit(LoginSecretTransferEvent.TotpCompleted(otp)) }
    }

    fun changeFromPushTo2FA() {
        val state = stateFlow.value as? LoginSecretTransferState.WaitForPush ?: return
        viewModelScope.launch { eventSharedFlow.emit(LoginSecretTransferEvent.ChangeFromPushTo2FA(state.secondFactor)) }
    }

    fun cancelOnError(error: LoginSecretTransferError) {
        when (error) {
            LoginSecretTransferError.QrCodeGeneration,
            LoginSecretTransferError.StartTransferError -> {
                viewModelScope.launch { stateFlow.emit(LoginSecretTransferState.Cancelled(stateFlow.value.data)) }
            }

            LoginSecretTransferError.LoginError -> viewModelScope.launch { eventSharedFlow.emit(LoginSecretTransferEvent.Cancel) }
        }
    }

    fun retry(error: LoginSecretTransferError) {
        when (error) {
            LoginSecretTransferError.QrCodeGeneration,
            LoginSecretTransferError.StartTransferError -> generateQrCode()

            LoginSecretTransferError.LoginError -> viewModelScope.launch { eventSharedFlow.emit(LoginSecretTransferEvent.Retry) }
        }
    }

    @VisibleForTesting
    fun generateQrCode() {
        flow {
            val response = mplessRequestTransferService.execute()
            emit(response.data.transferId)
        }
            .flowOn(ioDispatcher)
            .map { transferId ->
                val (publicKey, privateKey) = jniCryptography.generateX25519KeyPair()
                val secretTransferPublicKey = SecretTransferPublicKey(publicKey)
                startTransfer(transferId = transferId, secretTransferPublicKey, privateKey = privateKey)
                SecretTransferUri(transferId = transferId, publicKey = secretTransferPublicKey.raw)
            }
            .catch {
                stateFlow.emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.QrCodeGeneration))
            }
            .onEach { secretTransferUri ->
                stateFlow.emit(LoginSecretTransferState.QrCodeUriGenerated(stateFlow.value.data.copy(qrCodeUri = secretTransferUri.uri.toString())))
            }
            .onStart { stateFlow.emit(LoginSecretTransferState.LoadingQR(stateFlow.value.data.copy(qrCodeUri = null))) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun startTransfer(transferId: String, publicKey: SecretTransferPublicKey, privateKey: String) {
        flow { emit(startTransfer(transferId)) }
            .flowOn(ioDispatcher)
            .map { responseData -> parseResponseData(responseData = responseData, publicKey = publicKey, privateKey = privateKey) }
            .catch {
                stateFlow.emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.StartTransferError))
            }
            .onEach { mpTransferPayload ->
                waitForConfirmation(mpTransferPayload)
                stateFlow.emit(LoginSecretTransferState.ConfirmEmail(stateFlow.value.data, email = mpTransferPayload.login))
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    @VisibleForTesting
    fun waitForConfirmation(secretTransferPayload: SecretTransferPayload) {
        eventSharedFlow
            .flatMapMerge { event ->
                when (event) {
                    LoginSecretTransferEvent.Cancel -> {
                        flowOf(LoginSecretTransferState.Cancelled(data = stateFlow.value.data))
                    }

                    LoginSecretTransferEvent.Retry,
                    LoginSecretTransferEvent.EmailConfirmed -> {
                        verifyAccountType(secretTransferPayload)
                    }

                    is LoginSecretTransferEvent.TotpCompleted -> {
                        loginTotp(secretTransferPayload, event.otp)
                    }

                    is LoginSecretTransferEvent.ChangeFromPushTo2FA -> {
                        flowOf(LoginSecretTransferState.AskForTOTP(stateFlow.value.data, event.secondFactor))
                    }
                }
                    
                    .catch {
                        stateFlow.emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.LoginError))
                    }
            }
            .catch {
                stateFlow.emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.LoginError))
            }
            .onEach { state -> stateFlow.emit(state) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun verifyAccountType(secretTransferPayload: SecretTransferPayload): Flow<LoginSecretTransferState> {
        return flowOf(secretTransferPayload.vaultKey.type)
            .flatMapMerge { vaultKeyType ->
                when (vaultKeyType) {
                    SecretTransferPayload.Type.MASTER_PASSWORD -> {
                        when (val secondFactor = getAuthenticationSecondFactor(secretTransferPayload.login)) {
                            null,
                            is AuthenticationSecondFactor.EmailToken -> loginPassword(secretTransferPayload)

                            is AuthenticationSecondFactor.Totp -> {
                                if (secondFactor.isAuthenticatorEnabled) {
                                    loginAuthenticatorPush(secretTransferPayload, secondFactor)
                                } else {
                                    flowOf(LoginSecretTransferState.AskForTOTP(stateFlow.value.data, secondFactor))
                                }
                            }
                        }
                    }

                    SecretTransferPayload.Type.SSO -> loginSSO(secretTransferPayload)
                }
            }
    }

    @VisibleForTesting
    fun loginPassword(secretTransferPayload: SecretTransferPayload): Flow<LoginSecretTransferState> {
        return flow<LoginSecretTransferState> {
            val result = registrationWithAuthTicket(secretTransferPayload.login, secretTransferPayload.token)
            decryptAndCreateSession(secretTransferPayload = secretTransferPayload, result = result)
            emit(LoginSecretTransferState.LoginSuccess(stateFlow.value.data))
        }
            .catch {
                emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.LoginError))
            }
            .onStart { emit(LoginSecretTransferState.LoadingLogin(stateFlow.value.data)) }
    }

    @VisibleForTesting
    fun loginAuthenticatorPush(
        secretTransferPayload: SecretTransferPayload,
        secondFactor: AuthenticationSecondFactor.Totp
    ): Flow<LoginSecretTransferState> {
        return flow {
            val verificationResult = authenticationAuthTicketHelper.verifyDashlaneAuthenticator(secretTransferPayload.login)
            val result: AuthRegistrationAuthTicketService.Data = verificationResult.registerDevice()
            emit(result)
        }
            
            .takeUntil(eventSharedFlow.filter { it is LoginSecretTransferEvent.ChangeFromPushTo2FA })
            .map<AuthRegistrationAuthTicketService.Data, LoginSecretTransferState> { result ->
                decryptAndCreateSession(secretTransferPayload = secretTransferPayload, result = result)
                return@map LoginSecretTransferState.LoginSuccess(stateFlow.value.data)
            }
            .catch {
                emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.LoginError))
            }
            .onStart { emit(LoginSecretTransferState.WaitForPush(stateFlow.value.data, secondFactor)) }
    }

    @VisibleForTesting
    fun loginSSO(secretTransferPayload: SecretTransferPayload): Flow<LoginSecretTransferState> {
        return flow<LoginSecretTransferState> {
            val result = registrationWithAuthTicket(secretTransferPayload.login, secretTransferPayload.token)
            val username = Username.ofEmail(secretTransferPayload.login)
            val encryptedSettings = result.settings.content
            val encryptedRemoteKey = result.remoteKeys?.findByTypeOrNull(RemoteKey.Type.SSO)

            if (encryptedSettings == null || encryptedRemoteKey == null) throw AuthenticationInvalidSsoException()

            val ssoKey = AppKey.SsoKey(secretTransferPayload.vaultKey.value.decodeBase64ToByteArray())
            val localKey = authenticationLocalKeyRepository.createForRemote(
                username = username,
                appKey = ssoKey,
                cryptographyMarker = CryptographyMarker.Flexible.Defaults.noDerivation64
            )

            val remoteKey = authenticationSecretTransferRepository.decryptRemoteKey(ssoKey, encryptedRemoteKey)
            val settings = authenticationSecretTransferRepository.decryptSettings(remoteKey, encryptedSettings)
            val sessionResult = createSession(
                result = result,
                username = username,
                localKey = localKey,
                settings = settings,
                appKey = ssoKey,
                remoteKey = remoteKey,
                loginMode = LoginMode.Sso
            )

            if (sessionResult is SessionResult.Error) {
                throw LoginSsoContract.CannotStartSessionException(SESSION_ERROR_MESSAGE, sessionResult.cause)
            }

            userPreferencesManager.userSettingsBackupTimeMillis = result.settings.backupDate.epochMilli

            lockManager.unlock(LockPass.ofPassword(ssoKey))
            emit(LoginSecretTransferState.LoginSuccess(stateFlow.value.data))
        }
            .catch {
                emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.LoginError))
            }
            .onStart { emit(LoginSecretTransferState.LoadingLogin(stateFlow.value.data)) }
    }

    @VisibleForTesting
    fun loginTotp(secretTransferPayload: SecretTransferPayload, otp: String): Flow<LoginSecretTransferState> {
        return flow<LoginSecretTransferState> {
            val secondFactor = (stateFlow.value as? LoginSecretTransferState.AskForTOTP)?.secondFactor ?: throw AuthenticationInvalidTokenException()

            emit(LoginSecretTransferState.LoadingLogin(stateFlow.value.data))

            val response = secondFactoryRepository.validate(secondFactor, otp)
            val registeredUserDevice = response.registeredUserDevice as? RegisteredUserDevice.Remote ?: throw AuthenticationInvalidTokenException()

            val username = Username.ofEmail(secretTransferPayload.login)
            val password = AppKey.Password(secretTransferPayload.vaultKey.value, registeredUserDevice.serverKey)
            val localKey = authenticationLocalKeyRepository.createForRemote(
                username = username,
                appKey = password,
                cryptographyMarker = CryptographyMarker.Flexible.Defaults.argon2d
            )
            val settings = authenticationSecretTransferRepository.decryptSettings(password.toVaultKey(), registeredUserDevice.encryptedSettings)
            val sessionResult = sessionInitializer.createSession(
                username = username,
                accessKey = registeredUserDevice.accessKey,
                secretKey = registeredUserDevice.secretKey,
                localKey = localKey,
                userSettings = settings,
                sharingPublicKey = registeredUserDevice.sharingKeys?.publicKey,
                sharingPrivateKey = registeredUserDevice.sharingKeys?.encryptedPrivateKey,
                appKey = password,
                userAnalyticsId = registeredUserDevice.userAnalyticsId,
                deviceAnalyticsId = registeredUserDevice.deviceAnalyticsId,
                loginMode = LoginMode.MasterPassword()
            )

            userPreferencesManager.userSettingsBackupTimeMillis = registeredUserDevice.settingsDate.toEpochMilli()

            if (sessionResult is SessionResult.Error) {
                throw LoginSsoContract.CannotStartSessionException(SESSION_ERROR_MESSAGE, sessionResult.cause)
            }

            lockManager.unlock(LockPass.ofPassword(password))
            emit(LoginSecretTransferState.LoginSuccess(stateFlow.value.data))
        }
            .catch {
                emit(LoginSecretTransferState.Error(stateFlow.value.data, LoginSecretTransferError.LoginError))
            }
    }

    @VisibleForTesting
    suspend fun decryptAndCreateSession(
        secretTransferPayload: SecretTransferPayload,
        result: AuthRegistrationAuthTicketService.Data
    ) {
        val username = Username.ofEmail(secretTransferPayload.login)
        val encryptedSettings = result.settings.content ?: throw AuthenticationInvalidPasswordException()
        val password = AppKey.Password(secretTransferPayload.vaultKey.value)
        val localKey = authenticationLocalKeyRepository.createForRemote(
            username = username,
            appKey = password,
            cryptographyMarker = CryptographyMarker.Flexible.Defaults.argon2d
        )
        val settings = authenticationSecretTransferRepository.decryptSettings(password.toVaultKey(), encryptedSettings)
        val sessionResult = createSession(
            result = result,
            username = username,
            localKey = localKey,
            settings = settings,
            appKey = password,
            remoteKey = null,
            loginMode = LoginMode.MasterPassword()
        )

        userPreferencesManager.userSettingsBackupTimeMillis = result.settings.backupDate.epochMilli

        if (sessionResult is SessionResult.Error) {
            throw LoginSsoContract.CannotStartSessionException(SESSION_ERROR_MESSAGE, sessionResult.cause)
        }

        lockManager.unlock(LockPass.ofPassword(password))
    }

    @VisibleForTesting
    suspend fun registrationWithAuthTicket(login: String, token: String?): AuthRegistrationAuthTicketService.Data {
        userAccountStorage.saveSecuritySettings(username = login, securitySettings = UserSecuritySettings(isToken = true))
        val token = token ?: throw AuthenticationInvalidPasswordException()
        return authenticationSecretTransferRepository.register(login = login, token = token)
    }

    @VisibleForTesting
    suspend fun getAuthenticationSecondFactor(login: String): AuthenticationSecondFactor? {
        return when (val result = authenticationEmailRepository.getUserStatus(UnauthenticatedUser(login))) {
            is AuthenticationEmailRepository.Result.RequiresDeviceRegistration.SecondFactor -> result.secondFactor
            else -> null
        }
    }

    @VisibleForTesting
    fun parseResponseData(
        responseData: MplessStartTransferService.Data,
        publicKey: SecretTransferPublicKey,
        privateKey: String
    ): SecretTransferPayload {
        val symmetricKey: ByteArray = jniCryptography.deriveX25519SharedSecret(
            privateKey = privateKey,
            peerPublicKey = publicKey.toPeerPublicKey(responseData.publicKey),
            salt = SALT.decodeBase64ToByteArray(),
            sharedInfo = byteArrayOf(),
            derivedKeySize = 64
        ) ?: throw Exception()

        return authenticationSecretTransferRepository.decryptStartTransferResponse(symmetricKey, responseData.encryptedData)
            .let { moshi.adapter(SecretTransferPayload::class.java).fromJson(it) }
            ?: throw Exception()
    }

    private suspend fun startTransfer(transferId: String): MplessStartTransferService.Data = mplessStartTransferService.execute(
        MplessStartTransferService.Request(
            cryptography = MplessCryptography(
                ellipticCurve = MplessCryptography.EllipticCurve.X25519,
                algorithm = MplessCryptography.Algorithm.DIRECT_HKDF_SHA_256
            ),
            transferId = transferId
        )
    ).data

    private suspend fun createSession(
        result: AuthRegistrationAuthTicketService.Data,
        username: Username,
        localKey: LocalKey,
        settings: SyncObject.Settings,
        appKey: AppKey,
        remoteKey: VaultKey.RemoteKey?,
        loginMode: LoginMode
    ): SessionResult {
        return sessionInitializer.createSession(
            username = username,
            accessKey = result.deviceAccessKey,
            secretKey = result.deviceSecretKey,
            localKey = localKey,
            userSettings = settings,
            sharingPublicKey = result.sharingKeys?.publicKey,
            sharingPrivateKey = result.sharingKeys?.privateKey,
            appKey = appKey,
            remoteKey = remoteKey,
            userAnalyticsId = result.userAnalyticsId,
            deviceAnalyticsId = result.deviceAnalyticsId,
            loginMode = loginMode
        )
    }
}
