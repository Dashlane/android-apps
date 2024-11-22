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
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferError
import com.dashlane.secrettransfer.domain.SecretTransferException
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.SecretTransferPublicKey
import com.dashlane.secrettransfer.domain.SecretTransferUri
import com.dashlane.secrettransfer.generateExtraDeviceToken
import com.dashlane.secrettransfer.getPayload
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.authentication.Auth2faSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceTokenGeneratorService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.endpoints.mpless.MplessCompleteTransferService
import com.dashlane.server.api.endpoints.mpless.MplessCryptography
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import kotlinx.coroutines.launch
import javax.inject.Inject

const val SALT = "AXbCCLBYulWaVNWT/YfT+SiuhBOlFqLFaPPI5/8XIio="

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SecretTransferIntroViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val jniCryptography: JniCryptography,
    private val extraDeviceTokenGeneratorService: AuthRegistrationExtraDeviceTokenGeneratorService,
    private val mplessCompleteTransferService: MplessCompleteTransferService,
    private val cryptography: Cryptography,
    private val moshi: Moshi,
    private val auth2faSettingsService: Auth2faSettingsService,
    private val userAccountStorage: UserAccountStorage,
    private val secretTransferAnalytics: SecretTransferAnalytics,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<SecretTransferIntroState>(SecretTransferIntroState.Initial)
    val uiState = stateFlow.asStateFlow()

    init {
        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE)
    }

    fun scanClicked() {
        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SCAN_QR_CODE)
        viewModelScope.launch { stateFlow.emit(SecretTransferIntroState.ScanningQR) }
    }

    fun cancel() {
        viewModelScope.launch { stateFlow.emit(SecretTransferIntroState.Cancelled) }
    }

    fun deepLink(transferId: String, publicKey: String) {
        val secretTransferUri = SecretTransferUri(transferId = transferId, publicKey = publicKey)
        completeTransfer(secretTransferUri)
    }

    fun qrScanned(result: String?) {
        val secretTransferUri = runCatching { SecretTransferUri.fromUri(uri = Uri.parse(result)) }.getOrNull()
        secretTransferUri ?: run {
            viewModelScope.launch { stateFlow.emit(SecretTransferIntroState.Initial) }
            return
        }
        completeTransfer(secretTransferUri)
    }

    @VisibleForTesting
    fun completeTransfer(secretTransferUri: SecretTransferUri) {
        flow {
            val session = sessionManager.session ?: throw SecretTransferException(SecretTransferError.InvalidSession)
            emit(session)
        }
            .flatMapMerge { session ->
                get2FAStatusFlow(session)
                    .flatMapMerge { auth2faSettingsServiceData ->
                        when (auth2faSettingsServiceData.type) {
                            AuthSecurityType.TOTP_LOGIN,
                            AuthSecurityType.TOTP_DEVICE_REGISTRATION -> flowOf(null)

                            AuthSecurityType.EMAIL_TOKEN,
                            AuthSecurityType.SSO -> generateExtraDeviceTokenFlow(session)
                        }
                    }
                    .combine(generateKeysFlow(secretTransferUri)) { token, (symmetricKey, publicKey) ->
                        secretTransfer(session, secretTransferUri, token, symmetricKey, publicKey)
                    }
                    .map<Unit, SecretTransferIntroState> {
                        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SUCCESS)
                        SecretTransferIntroState.Success
                    }
            }
            .flowOn(ioDispatcher)
            .catch {
                secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_ERROR)
                emit(SecretTransferIntroState.Error((it as? SecretTransferException)?.error ?: SecretTransferError.Generic))
            }
            .onStart { emit(SecretTransferIntroState.Loading) }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun get2FAStatusFlow(session: Session): Flow<Auth2faSettingsService.Data> = flow {
        val response = auth2faSettingsService.execute(session.authorization)
        emit(response.data)
    }
        .catch { if (it is SecretTransferException) throw it else throw SecretTransferException(SecretTransferError.ServerError) }
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
            derivedKeySize = 64
        )?.toObfuscated() ?: throw SecretTransferException(SecretTransferError.CryptographicError)
        emit(symmetricKey to secretTransferPublicKey.raw)
    }
        .catch { throw SecretTransferException(SecretTransferError.CryptographicError) }
        .flowOn(defaultDispatcher)

    @VisibleForTesting
    fun generateExtraDeviceTokenFlow(session: Session) = flow<String> {
        val authorization = session.authorization
        val response = generateExtraDeviceToken(extraDeviceTokenGeneratorService, authorization)
        emit(response.token)
    }
        .catch {
            if (it is SecretTransferException) throw it else throw SecretTransferException(SecretTransferError.ServerError)
        }
        .flowOn(ioDispatcher)

    private suspend fun secretTransfer(
        session: Session,
        secretTransferUri: SecretTransferUri,
        token: String?,
        symmetricKey: ObfuscatedByteArray,
        publicKey: String
    ) {
        val userAccountInfo = userAccountStorage[session.username] ?: throw SecretTransferException(SecretTransferError.InvalidSession)

        val encryptedPayload = getPayload(
            email = session.username.email,
            appKey = session.appKey,
            userAccountInfo = userAccountInfo,
            token = token,
        )
            .let { payload -> moshi.adapter(SecretTransferPayload::class.java).toJson(payload) }
            .let { payloadJson -> encryptPayload(cryptography, symmetricKey = symmetricKey, payload = payloadJson) }

        completeTransfer(
            authorization = session.authorization,
            transferId = secretTransferUri.transferId,
            publicKey = publicKey,
            encryptedData = encryptedPayload.value
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
                cryptography = MplessCryptography(MplessCryptography.EllipticCurve.X25519, MplessCryptography.Algorithm.DIRECT_HKDF_SHA_256),
                publicKey = publicKey
            )
        )
    }
}
