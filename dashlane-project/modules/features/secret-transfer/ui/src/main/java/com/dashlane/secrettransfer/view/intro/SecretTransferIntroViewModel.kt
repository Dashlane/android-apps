package com.dashlane.secrettransfer.view.intro

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.EncryptedBase64String
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.encryptUtf8ToBase64String
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.cryptography.toObfuscated
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferError.CryptographicError
import com.dashlane.secrettransfer.domain.SecretTransferError.Generic
import com.dashlane.secrettransfer.domain.SecretTransferError.InvalidSession
import com.dashlane.secrettransfer.domain.SecretTransferError.ServerError
import com.dashlane.secrettransfer.domain.SecretTransferException
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.SecretTransferPublicKey
import com.dashlane.secrettransfer.domain.SecretTransferUri
import com.dashlane.secrettransfer.generateExtraDeviceToken
import com.dashlane.secrettransfer.getPayload
import com.dashlane.secrettransfer.view.intro.SecretTransferIntroState.SideEffect
import com.dashlane.secrettransfer.view.intro.SecretTransferIntroState.View
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.endpoints.authentication.Get2FAStatusService
import com.dashlane.server.api.endpoints.authentication.RequestExtraDeviceRegistrationService
import com.dashlane.server.api.endpoints.mpless.MplessCompleteTransferService
import com.dashlane.server.api.endpoints.mpless.MplessCryptography
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

const val SALT = "AXbCCLBYulWaVNWT/YfT+SiuhBOlFqLFaPPI5/8XIio="

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SecretTransferIntroViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val jniCryptography: JniCryptography,
    private val extraDeviceTokenGeneratorService: RequestExtraDeviceRegistrationService,
    private val mplessCompleteTransferService: MplessCompleteTransferService,
    private val cryptography: Cryptography,
    private val auth2faSettingsService: Get2FAStatusService,
    private val userAccountStorage: UserAccountStorage,
    private val secretTransferAnalytics: SecretTransferAnalytics,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<View, SideEffect>(View())
    val stateFlow: ViewStateFlow<View, SideEffect> = _stateFlow

    init {
        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE)
    }

    fun scanClicked() {
        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SCAN_QR_CODE)
        viewModelScope.launch { _stateFlow.send(SideEffect.ScanningQR) }
    }

    fun cancel() {
        viewModelScope.launch { _stateFlow.send(SideEffect.Cancelled) }
    }

    fun deepLink(transferId: String, publicKey: String) {
        val secretTransferUri = SecretTransferUri(transferId = transferId, publicKey = publicKey)
        completeTransfer(secretTransferUri)
    }

    fun qrScanned(result: String?) {
        val secretTransferUri = runCatching { SecretTransferUri.fromUri(uri = Uri.parse(result)) }.getOrNull()
        secretTransferUri ?: run {
            viewModelScope.launch { _stateFlow.update { state -> state.copy(isLoading = false) } }
            return
        }
        completeTransfer(secretTransferUri)
    }

    @VisibleForTesting
    fun completeTransfer(secretTransferUri: SecretTransferUri) {
        flow {
            val session = sessionManager.session ?: throw SecretTransferException(InvalidSession)
            emit(session)
        }
            .flatMapMerge { session ->
                get2FAStatusFlow(session)
                    .flatMapMerge { auth2faSettingsServiceData ->
                        when (auth2faSettingsServiceData.type) {
                            AuthSecurityType.TOTP_LOGIN,
                            AuthSecurityType.TOTP_DEVICE_REGISTRATION,
                            -> flowOf(null)

                            AuthSecurityType.EMAIL_TOKEN,
                            AuthSecurityType.SSO,
                            -> generateExtraDeviceTokenFlow(session)
                        }
                    }
                    .combine(generateKeysFlow(secretTransferUri)) { token, (symmetricKey, publicKey) ->
                        secretTransfer(session, secretTransferUri, token, symmetricKey, publicKey)
                    }
                    .map<Unit, SecretTransferIntroState> {
                        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SUCCESS)
                        SideEffect.Success
                    }
            }
            .flowOn(ioDispatcher)
            .catch {
                secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_ERROR)
                emit(_stateFlow.value.copy(isLoading = false, error = (it as? SecretTransferException)?.error ?: Generic))
            }
            .onStart { emit(_stateFlow.value.copy(isLoading = true)) }
            .onEach { state ->
                when (state) {
                    is View -> _stateFlow.update { state }
                    is SideEffect -> {
                        _stateFlow.update { it.copy(isLoading = false) }
                        if (state is SideEffect.Success) {
                            _stateFlow.update { it.copy(isSuccess = true) }
                            
                            delay(2_000)
                        }
                        _stateFlow.send(state)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun get2FAStatusFlow(session: Session): Flow<Get2FAStatusService.Data> = flow {
        val response = auth2faSettingsService.execute(session.authorization)
        emit(response.data)
    }
        .catch { if (it is SecretTransferException) throw it else throw SecretTransferException(ServerError) }
        .flowOn(ioDispatcher)

    @VisibleForTesting
    fun generateKeysFlow(secretTransferUri: SecretTransferUri): Flow<Pair<ObfuscatedByteArray, String>> = flow {
        val (publicKey, privateKey) = jniCryptography.generateX25519KeyPair()
        val secretTransferPublicKey = SecretTransferPublicKey(publicKey)
        val peerPublicKey = secretTransferPublicKey.toPeerPublicKey(secretTransferUri.publicKey)
        val symmetricKey = jniCryptography.deriveX25519SharedSecret(
            privateKey = privateKey,
            peerPublicKey = peerPublicKey,
            salt = SALT.decodeBase64ToByteArray(),
            sharedInfo = byteArrayOf(),
            derivedKeySize = 64,
        )?.toObfuscated() ?: throw SecretTransferException(CryptographicError)
        emit(symmetricKey to secretTransferPublicKey.raw)
    }
        .catch { throw SecretTransferException(CryptographicError) }
        .flowOn(defaultDispatcher)

    @VisibleForTesting
    fun generateExtraDeviceTokenFlow(session: Session) = flow<String> {
        val authorization = session.authorization
        val response = generateExtraDeviceToken(extraDeviceTokenGeneratorService, authorization)
        emit(response.token)
    }
        .catch {
            if (it is SecretTransferException) throw it else throw SecretTransferException(ServerError)
        }
        .flowOn(ioDispatcher)

    private suspend fun secretTransfer(
        session: Session,
        secretTransferUri: SecretTransferUri,
        token: String?,
        symmetricKey: ObfuscatedByteArray,
        publicKey: String,
    ) {
        val userAccountInfo = userAccountStorage[session.username] ?: throw SecretTransferException(InvalidSession)

        val encryptedPayload = getPayload(
            email = session.username.email,
            appKey = session.appKey,
            userAccountInfo = userAccountInfo,
            token = token,
        )
            .let { payload -> Json.encodeToString(SecretTransferPayload.serializer(), payload) }
            .let { payloadJson -> encryptPayload(cryptography, symmetricKey = symmetricKey, payload = payloadJson) }

        completeTransfer(
            authorization = session.authorization,
            transferId = secretTransferUri.transferId,
            publicKey = publicKey,
            encryptedData = encryptedPayload.value,
        )
    }

    @VisibleForTesting
    fun encryptPayload(cryptography: Cryptography, symmetricKey: ObfuscatedByteArray, payload: String): EncryptedBase64String {
        return symmetricKey.use { CryptographyKey.ofBytes64(it) }
            .use { cryptography.createFlexibleNoDerivation64EncryptionEngine(it) }
            .use { it.encryptUtf8ToBase64String(payload) }
    }

    @VisibleForTesting
    suspend fun completeTransfer(authorization: Authorization.User, transferId: String, publicKey: String, encryptedData: String) {
        mplessCompleteTransferService.execute(
            userAuthorization = authorization,
            request = MplessCompleteTransferService.Request(
                encryptedData = encryptedData,
                transferId = transferId,
                cryptography = MplessCryptography(
                    MplessCryptography.EllipticCurve.X25519,
                    MplessCryptography.Algorithm.DIRECT_HKDF_SHA_256,
                ),
                publicKey = publicKey,
            ),
        )
    }
}