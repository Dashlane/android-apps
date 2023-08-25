package com.dashlane.secrettransfer

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.encryptUtf8ToBase64String
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.cryptography.toObfuscated
import com.dashlane.login.pages.secrettransfer.LoginSecretTransferViewModel
import com.dashlane.login.pages.secrettransfer.SecretTransferPayload
import com.dashlane.login.pages.secrettransfer.SecretTransferPublicKey
import com.dashlane.login.pages.secrettransfer.SecretTransferUri
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.authentication.Auth2faSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceTokenGeneratorService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.endpoints.mpless.MplessCompleteTransferService
import com.dashlane.server.api.endpoints.mpless.MplessCryptography
import com.dashlane.session.AppKey
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class SecretTransferViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val jniCryptography: JniCryptography,
    private val extraDeviceTokenGeneratorService: AuthRegistrationExtraDeviceTokenGeneratorService,
    private val mplessCompleteTransferService: MplessCompleteTransferService,
    private val cryptography: Cryptography,
    private val moshi: Moshi,
    private val auth2faSettingsService: Auth2faSettingsService,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<SecretTransferState>(SecretTransferState.Initial)
    val uiState = stateFlow.asStateFlow()

    fun scanClicked() {
        viewModelScope.launch { stateFlow.emit(SecretTransferState.ScanningQR) }
    }

    fun cancel() {
        viewModelScope.launch { stateFlow.emit(SecretTransferState.Cancelled) }
    }

    fun deepLink(transferId: String, publicKey: String) {
        val secretTransferUri = SecretTransferUri(transferId = transferId, publicKey = publicKey)
        completeTransfer(secretTransferUri)
    }

    fun qrScanned(result: String?) {
        val secretTransferUri = runCatching { SecretTransferUri.fromUri(uri = Uri.parse(result)) }.getOrNull()
        secretTransferUri ?: run {
            viewModelScope.launch { stateFlow.emit(SecretTransferState.Initial) }
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
            }
            .flowOn(ioDispatcher)
            .catch {
                stateFlow.emit(SecretTransferState.Error((it as? SecretTransferException)?.error ?: SecretTransferError.Generic))
            }
            .onStart { stateFlow.emit(SecretTransferState.Loading) }
            .onEach { stateFlow.emit(SecretTransferState.Success) }
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
            salt = LoginSecretTransferViewModel.SALT.decodeBase64ToByteArray(),
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
        val response = generateExtraDeviceToken(authorization)
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
        val login = session.username.email
        val (type, value) = when (val appKey = session.appKey) {
            is AppKey.Password -> SecretTransferPayload.Type.MASTER_PASSWORD to appKey.passwordUtf8Bytes.decodeUtf8ToString()
            is AppKey.SsoKey -> SecretTransferPayload.Type.SSO to appKey.cryptographyKeyBytes.toByteArray().encodeBase64ToString()
        }
        val vaultKey = SecretTransferPayload.VaultKey(type, value)
        val secretTransferPayload = SecretTransferPayload(login = login, vaultKey = vaultKey, token = token)
        val jsonData = moshi.adapter(SecretTransferPayload::class.java).toJson(secretTransferPayload)

        val encryptedData = symmetricKey.use { CryptographyKey.ofBytes64(it) }
            .use { cryptography.createFlexibleNoDerivation64EncryptionEngine(it) }
            .use { it.encryptUtf8ToBase64String(jsonData) }

        mplessCompleteTransferService.execute(
            userAuthorization = session.authorization,
            request = MplessCompleteTransferService.Request(
                encryptedData = encryptedData.value,
                transferId = secretTransferUri.transferId,
                cryptography = MplessCryptography(MplessCryptography.EllipticCurve.X25519, MplessCryptography.Algorithm.DIRECT_HKDF_SHA_256),
                publicKey = publicKey
            )
        )
    }

    private suspend fun generateExtraDeviceToken(authorization: Authorization.User) = extraDeviceTokenGeneratorService.execute(
        userAuthorization = authorization,
        request = AuthRegistrationExtraDeviceTokenGeneratorService.Request(
            AuthRegistrationExtraDeviceTokenGeneratorService.Request.TokenType.SHORTLIVED
        )
    ).data

    private class SecretTransferException(val error: SecretTransferError) : Exception()
}
