package com.dashlane.secrettransfer.qrcode

import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferPublicKey
import com.dashlane.secrettransfer.generateKeySet
import com.dashlane.secrettransfer.startTransfer
import com.dashlane.server.api.endpoints.mpless.MplessRequestTransferService
import com.dashlane.server.api.endpoints.mpless.MplessStartTransferService
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _stateFlow = MutableViewStateFlow<QrCodeState.View, QrCodeState.SideEffect>(QrCodeState.View(isLoading = true))
    val stateFlow: ViewStateFlow<QrCodeState.View, QrCodeState.SideEffect> = _stateFlow

    fun viewStarted(email: String?, qrCodeSize: Int, @ColorInt qrCodeColor: Int) {
        secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_QR_CODE)
        if (email != null) {
            viewModelScope.launch {
                _stateFlow.update { state ->
                    state.copy(email = email)
                }
            }
        }
        if (_stateFlow.viewState.value.qrCodeBitmap == null) {
            generateQrCode(qrCodeSize = qrCodeSize, qrCodeColor = qrCodeColor)
        }
    }

    fun helpClicked() {
        secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_HELP)
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(bottomSheetVisible = true)
            }
        }
    }

    fun bottomSheetDismissed() {
        viewModelScope.launch {
            _stateFlow.update { state ->
                state.copy(
                    bottomSheetVisible = false
                )
            }
        }
    }

    fun cancelOnError(error: QrCodeError) {
        when (error) {
            QrCodeError.QrCodeGeneration,
            QrCodeError.StartTransferError -> {
                viewModelScope.launch {
                    _stateFlow.send(
                        QrCodeState.SideEffect.Cancelled
                    )
                }
            }
        }
    }

    fun retry(error: QrCodeError, qrCodeSize: Int, @ColorInt qrCodeColor: Int) {
        when (error) {
            QrCodeError.QrCodeGeneration,
            QrCodeError.StartTransferError -> generateQrCode(qrCodeSize = qrCodeSize, qrCodeColor = qrCodeColor)
        }
    }

    fun arkClicked() {
        viewModelScope.launch {
            val email = _stateFlow.value.email ?: throw IllegalStateException("email cannot be null")
            val accessKey = deviceInfoRepository.deviceId ?: throw IllegalStateException("deviceId cannot be null")
            val registeredUserDevice = RegisteredUserDevice.Local(login = email, securityFeatures = emptySet(), accessKey = accessKey)
            _stateFlow.update { state ->
                state.copy(bottomSheetVisible = false)
            }
            _stateFlow.send(
                QrCodeState.SideEffect.GoToARK(registeredUserDevice)
            )
        }
    }

    fun universalD2DClicked() {
        viewModelScope.launch {
            val email = _stateFlow.value.email ?: throw IllegalStateException("email cannot be null")
            _stateFlow.update { state ->
                state.copy(bottomSheetVisible = false)
            }
            _stateFlow.send(QrCodeState.SideEffect.GoToUniversalD2D(email))
        }
    }

    @VisibleForTesting
    fun generateQrCode(qrCodeSize: Int, @ColorInt qrCodeColor: Int) {
        flow {
            val keySet = generateKeySet(jniCryptography = jniCryptography, mplessRequestTransferService = mplessRequestTransferService)
            startTransfer(transferId = keySet.transferId, keySet.publicKey, privateKey = keySet.privateKey)
            emit(
                _stateFlow.value.copy(
                    qrCodeBitmap = generateQrCodeBitmap(uri = keySet.secretTransferUri.uri, size = qrCodeSize, color = qrCodeColor),
                    error = null,
                    isLoading = false
                )
            )
        }
            .flowOn(defaultDispatcher)
            .catch {
                emit(_stateFlow.value.copy(error = QrCodeError.QrCodeGeneration, isLoading = false))
            }
            .onEach { state -> _stateFlow.update { state } }
            .onStart { emit(QrCodeState.View(isLoading = true)) }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun startTransfer(transferId: String, publicKey: SecretTransferPublicKey, privateKey: String) {
        flow<QrCodeState> {
            val secretTransferPayload = startTransfer(
                jniCryptography = jniCryptography,
                mplessStartTransferService = mplessStartTransferService,
                authenticationSecretTransferRepository = authenticationSecretTransferRepository,
                moshi = moshi,
                publicKey = publicKey,
                privateKey = privateKey,
                transferId = transferId
            )
            emit(QrCodeState.SideEffect.GoToConfirmEmail(secretTransferPayload))
        }
            .flowOn(ioDispatcher)
            .catch {
                emit(_stateFlow.value.copy(error = QrCodeError.StartTransferError, isLoading = false))
            }
            .onEach { state ->
                when (state) {
                    is QrCodeState.SideEffect -> _stateFlow.send(state)
                    is QrCodeState.View -> _stateFlow.update { state }
                }
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }
}
