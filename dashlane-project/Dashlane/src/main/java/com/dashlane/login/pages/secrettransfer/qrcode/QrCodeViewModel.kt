package com.dashlane.login.pages.secrettransfer.qrcode

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.SecretTransferPublicKey
import com.dashlane.secrettransfer.domain.SecretTransferUri
import com.dashlane.server.api.endpoints.mpless.MplessCryptography
import com.dashlane.server.api.endpoints.mpless.MplessRequestTransferService
import com.dashlane.server.api.endpoints.mpless.MplessStartTransferService
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
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
    private val deviceInfoRepository: DeviceInfoRepository,
    private val secretTransferAnalytics: SecretTransferAnalytics,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<QrCodeState>(QrCodeState.LoadingQR(QrCodeData()))

    val uiState = stateFlow.asStateFlow()

    companion object {
        
        
        const val SALT = "AXbCCLBYulWaVNWT/YfT+SiuhBOlFqLFaPPI5/8XIio="
    }

    fun viewStarted(email: String?) {
        secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_QR_CODE)
        if (email != null) {
            viewModelScope.launch { stateFlow.emit(QrCodeState.Initial(stateFlow.value.data.copy(email = email))) }
        }
        if (stateFlow.value.data.qrCodeUri == null) generateQrCode()
    }

    fun viewNavigated() {
        viewModelScope.launch { stateFlow.emit(QrCodeState.Initial(stateFlow.value.data.copy(qrCodeUri = null))) }
    }

    fun helpClicked() {
        secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_HELP)
        viewModelScope.launch { stateFlow.emit(QrCodeState.QrCodeUriGenerated(stateFlow.value.data.copy(bottomSheetVisible = true))) }
    }

    fun bottomSheetDismissed() {
        viewModelScope.launch {
            val newData = stateFlow.value.data.copy(bottomSheetVisible = false)
            val newState = when (val state = stateFlow.value) {
                is QrCodeState.Error -> QrCodeState.Error(newData, state.error)
                else -> QrCodeState.QrCodeUriGenerated(newData)
            }
            stateFlow.emit(newState)
        }
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
            val email = stateFlow.value.data.email ?: throw IllegalStateException("email cannot be null")
            val accessKey = deviceInfoRepository.deviceId ?: throw IllegalStateException("deviceId cannot be null")
            val registeredUserDevice = RegisteredUserDevice.Local(login = email, securityFeatures = emptySet(), accessKey = accessKey)
            stateFlow.emit(QrCodeState.GoToARK(stateFlow.value.data.copy(bottomSheetVisible = false), registeredUserDevice))
        }
    }

    fun universalD2DClicked() {
        viewModelScope.launch {
            val email = stateFlow.value.data.email ?: throw IllegalStateException("email cannot be null")
            stateFlow.emit(QrCodeState.GoToUniversalD2D(stateFlow.value.data.copy(bottomSheetVisible = false), email))
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
