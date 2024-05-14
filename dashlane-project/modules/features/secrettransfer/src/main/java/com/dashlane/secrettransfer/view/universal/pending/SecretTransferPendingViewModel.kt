package com.dashlane.secrettransfer.view.universal.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.network.tools.authorization
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.nitro.cryptography.sodium.keys.KeyExchangeKeyPair
import com.dashlane.nitro.cryptography.sodium.keys.ServerKeyPair
import com.dashlane.passphrase.generator.PassphraseGenerator
import com.dashlane.secrettransfer.SecretTransferError
import com.dashlane.secrettransfer.SecretTransferException
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.generateExtraDeviceToken
import com.dashlane.secrettransfer.generatePassphraseWithMissingWord
import com.dashlane.secrettransfer.getPayload
import com.dashlane.secrettransfer.view.SecretTransfer
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceTokenGeneratorService
import com.dashlane.server.api.endpoints.secrettransfer.CompleteTransferService
import com.dashlane.server.api.endpoints.secrettransfer.StartSenderKeyExchangeService
import com.dashlane.server.api.endpoints.secrettransfer.TransferType
import com.dashlane.server.api.endpoints.secrettransfer.exceptions.TransferExpiredException
import com.dashlane.server.api.exceptions.DashlaneApiHttp504Exception
import com.dashlane.session.SessionManager
import com.dashlane.ui.widgets.compose.Passphrase
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

@HiltViewModel
class SecretTransferPendingViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val moshi: Moshi,
    private val sodiumCryptography: SodiumCryptography,
    private val passphraseGenerator: PassphraseGenerator,
    private val startSenderKeyExchangeService: StartSenderKeyExchangeService,
    private val completeTransferService: CompleteTransferService,
    private val extraDeviceTokenGeneratorService: AuthRegistrationExtraDeviceTokenGeneratorService,
    private val secretTransferAnalytics: SecretTransferAnalytics,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val stateFlow = MutableStateFlow<SecretTransferPendingState>(SecretTransferPendingState.Initial(SecretTransferPendingData()))
    val uiState = stateFlow.asStateFlow()

    private lateinit var senderSessionKeys: ServerKeyPair

    fun confirmTransfer(transfer: SecretTransfer) {
        flow {
            val keyPair = sodiumCryptography.generateKeyExchangeKeyPair() ?: throw Exception()
            val session = sessionManager.session ?: throw SecretTransferException(SecretTransferError.InvalidSession)

            
            val response = startSenderKeyExchange(
                authorization = session.authorization,
                senderPublicKey = keyPair.publicKey.encodeBase64ToString(),
                transferId = transfer.id
            )
            emit(Pair(keyPair, response.receiverPublicKey.decodeBase64ToByteArray()))
        }
            .map<Pair<KeyExchangeKeyPair, ByteArray>, SecretTransferPendingState> { (keypair, receiverPublicKey) ->
                val session = sessionManager.session ?: throw SecretTransferException(SecretTransferError.InvalidSession)
                val login = session.username.email

                verifyHash(receiverPublicKey = receiverPublicKey, receiverHashedPublicKey = transfer.hashedPublicKey)

                senderSessionKeys = sodiumCryptography.keyExchangeServerSessionKeys(keypair, receiverPublicKey) ?: throw Exception()

                val passphraseViewData = generatePassphraseWithMissingWord(
                    passphraseGenerator = passphraseGenerator,
                    sodiumCryptography = sodiumCryptography,
                    sessionKey = senderSessionKeys.tx,
                    login = login,
                    transferId = transfer.id
                )

                secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SECURITY_CHALLENGE)

                return@map SecretTransferPendingState.PassphraseVerification(stateFlow.value.data.copy(passphrase = passphraseViewData))
            }
            .flowOn(ioDispatcher)
            .catch {
                val error = when (it) {
                    is DashlaneApiHttp504Exception,
                    is TransferExpiredException -> {
                        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_TIMEOUT)
                        SecretTransferPendingError.Timeout
                    }
                    else -> {
                        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_ERROR)
                        SecretTransferPendingError.Generic
                    }
                }
                emit(SecretTransferPendingState.Error(stateFlow.value.data, error = error))
            }
            .onStart { emit(SecretTransferPendingState.LoadingChallenge(stateFlow.value.data.copy(transfer = transfer))) }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
    }

    fun missingWordValueChanged(missingWord: String) {
        if (stateFlow.value is SecretTransferPendingState.LoadingAccount) return
        viewModelScope.launch {
            val updatedPassphrase = stateFlow.value.data.passphrase
                ?.map { if (it is Passphrase.Missing) it.copy(userInput = missingWord) else it }
            stateFlow.emit(SecretTransferPendingState.PassphraseVerification(stateFlow.value.data.copy(passphrase = updatedPassphrase)))
        }
    }

    fun rejectTransfer() {
        viewModelScope.launch {
            stateFlow.emit(SecretTransferPendingState.Reject(stateFlow.value.data))
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            val newState = when (stateFlow.value) {
                is SecretTransferPendingState.Cancelled,
                is SecretTransferPendingState.Error,
                is SecretTransferPendingState.Initial,
                is SecretTransferPendingState.Reject,
                is SecretTransferPendingState.Success,
                -> SecretTransferPendingState.Cancelled(stateFlow.value.data.copy())
                is SecretTransferPendingState.CancelPassphrase,
                is SecretTransferPendingState.LoadingChallenge,
                is SecretTransferPendingState.LoadingAccount,
                is SecretTransferPendingState.PassphraseVerification -> SecretTransferPendingState.CancelPassphrase(stateFlow.value.data.copy())
            }
            stateFlow.emit(newState)
        }
    }

    fun cancelDismiss() {
        viewModelScope.launch {
            stateFlow.emit(SecretTransferPendingState.PassphraseVerification(stateFlow.value.data.copy()))
        }
    }

    fun cancelConfirm() {
        viewModelScope.launch {
            stateFlow.emit(SecretTransferPendingState.Cancelled(stateFlow.value.data.copy()))
        }
    }

    @Suppress("LongMethod")
    fun completeTransfer() {
        flow {
            val data = stateFlow.value.data

            
            val missingWord = data.passphrase?.filterIsInstance<Passphrase.Missing>()?.first()
            val userInput = missingWord?.userInput?.replace(" ", "")
            if (missingWord == null || missingWord.value != userInput) {
                val updatedPassphrase = data.passphrase?.map { if (it is Passphrase.Missing) it.copy(isError = true) else it }
                val passphraseTries = data.passphraseTries + 1
                if (passphraseTries == 3) {
                    secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_ATTEMPT_LIMIT_REACHED)
                    emit(SecretTransferPendingState.Error(data, SecretTransferPendingError.PassphraseMaxTries))
                } else {
                    emit(
                        SecretTransferPendingState.PassphraseVerification(
                            data.copy(
                                passphrase = updatedPassphrase,
                                passphraseTries = passphraseTries
                            )
                        )
                    )
                }
                return@flow
            }

            emit(SecretTransferPendingState.LoadingAccount(data))

            val transfer = data.transfer ?: throw SecretTransferException(SecretTransferError.InvalidSession)
            val session = sessionManager.session ?: throw SecretTransferException(SecretTransferError.InvalidSession)
            val userAccountInfo = userAccountStorage[session.username] ?: throw SecretTransferException(SecretTransferError.InvalidSession)
            val login = session.username.email

            val (senderSecretKey, nonce) = generateSymmetricKeyAndNonce(
                senderSessionKeys = senderSessionKeys,
                login = login,
                transferId = transfer.id
            )
            val token = generateExtraDeviceToken(extraDeviceTokenGeneratorService, session.authorization).token
            val payload = getPayload(
                email = login,
                appKey = session.appKey,
                userAccountInfo = userAccountInfo,
                token = token,
            )
            val payloadJson = moshi.adapter(SecretTransferPayload::class.java).toJson(payload)
            val encryptedPayload = sodiumCryptography.secretboxEasy(payloadJson.encodeUtf8ToByteArray(), nonce, senderSecretKey)
                ?: throw SecretTransferException(SecretTransferError.CryptographicError)

            
            coroutineScope {
                launch {
                    completeTransfer(
                        authorization = session.authorization,
                        transferId = transfer.id,
                        encryptedData = encryptedPayload.encodeBase64ToString(),
                        nonce = nonce.encodeBase64ToString()
                    )
                }
                launch { delay(500) }
            }

            secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_SUCCESS)
            emit(SecretTransferPendingState.Success(stateFlow.value.data))
        }
            .flowOn(ioDispatcher)
            .catch {
                val error = when (it) {
                    is DashlaneApiHttp504Exception,
                    is TransferExpiredException -> {
                        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_TIMEOUT)
                        SecretTransferPendingError.Timeout
                    }
                    else -> {
                        secretTransferAnalytics.pageView(AnyPage.SETTINGS_ADD_NEW_DEVICE_ERROR)
                        SecretTransferPendingError.Generic
                    }
                }
                emit(SecretTransferPendingState.Error(stateFlow.value.data, error))
            }
            .onEach { state ->
                stateFlow.emit(state)
            }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    fun generateSymmetricKeyAndNonce(
        senderSessionKeys: ServerKeyPair,
        login: String,
        transferId: String
    ): Pair<ByteArray, ByteArray> {
        val message = "${SodiumCryptography.ENCRYPTION_KEY_HEADER}${login.length}$login$transferId".encodeUtf8ToByteArray()
        val senderSecretKey = sodiumCryptography.genericHash(message, senderSessionKeys.tx)
        val nonce = sodiumCryptography.randomBuf(SodiumCryptography.SODIUM_NONCE_BYTES)
        return Pair(senderSecretKey, nonce)
    }

    @VisibleForTesting
    fun verifyHash(receiverPublicKey: ByteArray, receiverHashedPublicKey: String) {
        val receiverPublicKeyHash = sodiumCryptography.genericHash(receiverPublicKey)
        val areHashesTheSame = sodiumCryptography.sodiumMemcmp(receiverHashedPublicKey.decodeBase64ToByteArray(), receiverPublicKeyHash)
        if (!areHashesTheSame) throw SecretTransferException(SecretTransferError.CryptographicError)
    }

    @VisibleForTesting
    suspend fun startSenderKeyExchange(
        authorization: Authorization.User,
        senderPublicKey: String,
        transferId: String
    ): StartSenderKeyExchangeService.Data {
        val request = StartSenderKeyExchangeService.Request(senderPublicKey = senderPublicKey, transferId = transferId)
        val response = startSenderKeyExchangeService.execute(userAuthorization = authorization, request = request)
        return response.data
    }

    @VisibleForTesting
    suspend fun completeTransfer(authorization: Authorization.User, transferId: String, encryptedData: String, nonce: String) {
        val transfer = CompleteTransferService.Request.Transfer(
            encryptedData = encryptedData,
            transferType = TransferType.UNIVERSAL,
            transferId = transferId,
            nonce = nonce
        )
        val request = CompleteTransferService.Request(transfer)
        completeTransferService.execute(userAuthorization = authorization, request = request)
    }
}
