package com.dashlane.wear.login.remote.qr

import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.cryptography.jni.JniCryptography
import com.dashlane.data.login.AuthenticationSecondFactor
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.data.login.SecurityFeature
import com.dashlane.data.login.UnauthenticatedUser
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.domain.SecretTransferPublicKey
import com.dashlane.secrettransfer.generateKeySet
import com.dashlane.secrettransfer.qrcode.generateQrCodeBitmap
import com.dashlane.secrettransfer.startTransfer
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.server.api.endpoints.mpless.MplessRequestTransferService
import com.dashlane.server.api.endpoints.mpless.MplessStartTransferService
import com.dashlane.server.api.exceptions.DashlaneApiOfflineException
import com.dashlane.user.UserSecuritySettings
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.wear.login.WearLoginRepository
import com.dashlane.wear.login.remote.qr.WearQrCodeState.SideEffect.GoToAuthorization
import com.dashlane.wear.login.remote.qr.WearQrCodeState.SideEffect.GoToOtpChallenge
import com.dashlane.wear.login.remote.qr.WearQrCodeState.View.Error
import com.dashlane.wear.login.remote.qr.WearQrCodeState.View.Loading
import com.dashlane.wear.login.remote.qr.WearQrCodeState.View.QrCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WearQrCodeViewModel @Inject constructor(
    private val loginRepository: WearLoginRepository,
    private val jniCryptography: JniCryptography,
    private val userAccountStorage: UserAccountStorage,
    private val mplessRequestTransferService: MplessRequestTransferService,
    private val mplessStartTransferService: MplessStartTransferService,
    private val authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    private val authenticationEmailRepository: AuthenticationEmailRepository,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _stateFlow =
        MutableViewStateFlow<WearQrCodeState.View, WearQrCodeState.SideEffect>(Loading)
    val stateFlow: ViewStateFlow<WearQrCodeState.View, WearQrCodeState.SideEffect> = _stateFlow

    fun generateQrCode(
        qrCodeSize: Int,
        @ColorInt qrCodeColor: Int,
    ) {
        flow<WearQrCodeState.View> {
            loginRepository.resetRepository()
            val keySet = generateKeySet(
                jniCryptography = jniCryptography,
                mplessRequestTransferService = mplessRequestTransferService,
            )
            startTransfer(
                transferId = keySet.transferId,
                keySet.publicKey,
                privateKey = keySet.privateKey,
            )
            emit(
                QrCode(
                    qrCodeBitmap = generateQrCodeBitmap(
                        uri = keySet.secretTransferUri.uri,
                        size = qrCodeSize,
                        color = qrCodeColor,
                        marginSize = 1,
                    ),
                ),
            )
        }
            .flowOn(ioDispatcher)
            .onStart { emit(Loading) }
            .catch {
                emit(
                    Error(
                        error = QrCodeError.QrCodeGenerationError(
                            isDeviceOffline = it is DashlaneApiOfflineException,
                        ),
                    ),
                )
            }
            .flowOn(defaultDispatcher)
            .onEach { state -> _stateFlow.update { state } }
            .launchIn(viewModelScope)
    }

    fun emailConfirmed(secretTransferPayload: SecretTransferPayload) {
        verifyAccountType(secretTransferPayload)
    }

    private fun startTransfer(
        transferId: String,
        publicKey: SecretTransferPublicKey,
        privateKey: String,
    ) {
        flow<WearQrCodeState> {
            val secretTransferPayload = startTransfer(
                jniCryptography = jniCryptography,
                mplessStartTransferService = mplessStartTransferService,
                authenticationSecretTransferRepository = authenticationSecretTransferRepository,
                publicKey = publicKey,
                privateKey = privateKey,
                transferId = transferId,
            )
            loginRepository.updateSecretTransferPayload(secretTransferPayload)
            emit(WearQrCodeState.View.EmailConfirmation(secretTransferPayload))
        }
            .flowOn(ioDispatcher)
            .catch {
                emit(
                    Error(error = QrCodeError.StartTransferError),
                )
            }
            .onEach { state ->
                when (state) {
                    is WearQrCodeState.SideEffect -> _stateFlow.send(state)
                    is WearQrCodeState.View -> _stateFlow.update { state }
                }
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    private fun verifyAccountType(secretTransferPayload: SecretTransferPayload) {
        flow<WearQrCodeState.View> {
            when (secretTransferPayload.vaultKey.type) {
                SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD,
                SecretTransferPayload.Type.MASTER_PASSWORD,
                -> {
                    when (
                        val secondFactor =
                            getAuthenticationSecondFactor(secretTransferPayload.login)
                    ) {
                        is AuthenticationSecondFactor.EmailToken -> {
                            val registeredUserDevice = registrationWithAuthTicket(
                                login = secretTransferPayload.login,
                                token = secretTransferPayload.token
                                    ?: throw IllegalStateException(),
                                securityFeatures = secondFactor.securityFeatures,
                                remoteKeyType = RemoteKey.Type.MASTER_PASSWORD,
                            )
                            loginRepository.updateRegisteredUserDevice(registeredUserDevice = registeredUserDevice)
                            _stateFlow.send(GoToAuthorization)
                        }

                        is AuthenticationSecondFactor.Totp -> {
                            _stateFlow.send(GoToOtpChallenge)
                        }

                        else -> {
                            error("Invalid second factor")
                        }
                    }
                }

                SecretTransferPayload.Type.SSO -> {
                    error("SSO account not supported")
                }
            }
        }
            .catch { e ->
                emit(
                    Error(error = QrCodeError.AccountVerificationError(e)),
                )
            }
            .onEach { state ->
                _stateFlow.update { state }
            }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    private suspend fun registrationWithAuthTicket(
        login: String,
        token: String,
        securityFeatures: Set<SecurityFeature>,
        remoteKeyType: RemoteKey.Type,
    ): RegisteredUserDevice.Remote {
        userAccountStorage.saveSecuritySettings(
            username = login,
            securitySettings = UserSecuritySettings(isToken = true),
        )
        return authenticationSecretTransferRepository.register(
            login = login,
            securityFeatures = securityFeatures,
            token = token,
            remoteKeyType = remoteKeyType,
        )
    }

    private suspend fun getAuthenticationSecondFactor(login: String): AuthenticationSecondFactor? {
        return when (
            val result =
                authenticationEmailRepository.getUserStatus(UnauthenticatedUser(login))
        ) {
            is AuthenticationEmailRepository.Result.RequiresDeviceRegistration.SecondFactor -> result.secondFactor
            else -> null
        }
    }
}