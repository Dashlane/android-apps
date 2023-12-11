package com.dashlane.login.pages.secrettransfer.qrcode

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.login.pages.secrettransfer.SecretTransferPayload
import com.dashlane.login.pages.secrettransfer.SecretTransferPublicKey
import com.dashlane.login.pages.secrettransfer.SecretTransferUri
import com.dashlane.server.api.endpoints.accountrecovery.AccountRecoveryGetStatusService
import com.dashlane.server.api.endpoints.mpless.MplessCryptography
import com.dashlane.server.api.endpoints.mpless.MplessRequestTransferService
import com.dashlane.server.api.endpoints.mpless.MplessStartTransferService
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel
class QrCodeViewModel @Inject constructor(
    private val moshi: Moshi,
    private val jniCryptography: JniCryptography,
    private val mplessRequestTransferService: MplessRequestTransferService,
    private val mplessStartTransferService: MplessStartTransferService,
    private val authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    private val accountRecoveryGetStatusService: AccountRecoveryGetStatusService,
    private val deviceInfoRepository: DeviceInfoRepository,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<QrCodeState>(QrCodeState.LoadingQR(QrCodeData()))

    val uiState = stateFlow.asStateFlow()

    companion object {
        
        
        const val SALT = "AXbCCLBYulWaVNWT/YfT+SiuhBOlFqLFaPPI5/8XIio="
    }

    fun viewStarted(email: String?) {
        if (stateFlow.value.data.qrCodeUri == null) generateQrCode()
        if (email != null) checkARKStatus(email)
    }

    fun hasNavigated() {
        viewModelScope.launch { stateFlow.emit(QrCodeState.Initial(stateFlow.value.data.copy(qrCodeUri = null))) }
    }

    fun cancelOnError(error: QrCodeError) {
        when (error) {
            QrCodeError.QrCodeGeneration,
            QrCodeError.StartTransferError -> {
                viewModelScope.launch { stateFlow.emit(QrCodeState.Cancelled(stateFlow.value.data)) }
            }
        }
    }

    fun retry(error: QrCodeError) {
        when (error) {
            QrCodeError.QrCodeGeneration,
            QrCodeError.StartTransferError -> generateQrCode()
        }
    }

    fun arkClicked() {
        viewModelScope.launch {
            val email = stateFlow.value.data.email ?: return@launch
            val accessKey = deviceInfoRepository.deviceId ?: return@launch
            val registeredUserDevice = RegisteredUserDevice.Local(login = email, securityFeatures = emptySet(), accessKey = accessKey)
            stateFlow.emit(QrCodeState.GoToARK(stateFlow.value.data, registeredUserDevice))
        }
    }

    @VisibleForTesting
    fun generateQrCode() {
        flow {
            val response = mplessRequestTransferService.execute()
            emit(response.data.transferId)
        }
            .flowOn(ioDispatcher)
            .map<String, QrCodeState> { transferId ->
                val (publicKey, privateKey) = jniCryptography.generateX25519KeyPair()
                val secretTransferPublicKey = SecretTransferPublicKey(publicKey)
                startTransfer(transferId = transferId, secretTransferPublicKey, privateKey = privateKey)
                val secretTransferUri = SecretTransferUri(transferId = transferId, publicKey = secretTransferPublicKey.raw)
                QrCodeState.QrCodeUriGenerated(stateFlow.value.data.copy(qrCodeUri = secretTransferUri.uri.toString()))
            }
            .flowOn(defaultDispatcher)
            .catch {
                emit(QrCodeState.Error(stateFlow.value.data, QrCodeError.QrCodeGeneration))
            }
            .onEach { state -> stateFlow.emit(state) }
            .onStart { emit(QrCodeState.LoadingQR(stateFlow.value.data.copy(qrCodeUri = null))) }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun checkARKStatus(email: String) {
        flow<QrCodeState> {
            val request = AccountRecoveryGetStatusService.Request(email)
            val getStatusResponse = accountRecoveryGetStatusService.execute(request)
            emit(QrCodeState.QrCodeUriGenerated(stateFlow.value.data.copy(email = email, arkEnabled = getStatusResponse.data.enabled)))
        }
            .flowOn(ioDispatcher)
            .catch {
                
            }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun startTransfer(transferId: String, publicKey: SecretTransferPublicKey, privateKey: String) {
        flow<QrCodeState> {
            val responseData = startTransfer(transferId)
            val secretTransferPayload = parseResponseData(responseData = responseData, publicKey = publicKey, privateKey = privateKey)
            emit(QrCodeState.GoToConfirmEmail(stateFlow.value.data, secretTransferPayload))
        }
            .flowOn(ioDispatcher)
            .catch {
                emit(QrCodeState.Error(stateFlow.value.data, QrCodeError.StartTransferError))
            }
            .onEach { state -> stateFlow.emit(state) }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
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
            .let { moshi.adapter(SecretTransferPayload::class.java).fromJson(it) } ?: throw Exception()
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
}
