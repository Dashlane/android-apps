package com.dashlane.login.pages.secrettransfer.universal.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.user.UserSecuritySettings
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.login.AuthenticationSecretTransferRepository
import com.dashlane.cryptography.decodeBase64ToByteArray
import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.cryptography.encodeBase64ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.nitro.cryptography.sodium.keys.ClientKeyPair
import com.dashlane.passphrase.generator.PassphraseGenerator
import com.dashlane.secrettransfer.domain.SecretTransferAnalytics
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.secrettransfer.generatePassphrase
import com.dashlane.server.api.endpoints.authentication.RemoteKey
import com.dashlane.server.api.endpoints.secrettransfer.CompleteKeyExchangeService
import com.dashlane.server.api.endpoints.secrettransfer.RequestTransferService
import com.dashlane.server.api.endpoints.secrettransfer.StartReceiverKeyExchangeService
import com.dashlane.server.api.endpoints.secrettransfer.StartTransferService
import com.dashlane.server.api.endpoints.secrettransfer.TransferType
import com.dashlane.server.api.exceptions.DashlaneApiHttp504Exception
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@HiltViewModel
class UniversalIntroViewModel @Inject constructor(
    private val requestTransferService: RequestTransferService,
    private val startReceiverKeyExchangeService: StartReceiverKeyExchangeService,
    private val completeKeyExchangeService: CompleteKeyExchangeService,
    private val startTransferService: StartTransferService,
    private val sodiumCryptography: SodiumCryptography,
    private val passphraseGenerator: PassphraseGenerator,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val userAccountStorage: UserAccountStorage,
    private val authenticationSecretTransferRepository: AuthenticationSecretTransferRepository,
    private val moshi: Moshi,
    private val secretTransferAnalytics: SecretTransferAnalytics,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val stateFlow = MutableStateFlow<UniversalIntroState>(UniversalIntroState.Initial(UniversalIntroData()))

    val uiState = stateFlow.asStateFlow()

    fun viewStarted(email: String?) {
        if (stateFlow.value !is UniversalIntroState.Success && email != null) secretTransfer(email)
    }

    fun viewNavigated() {
        viewModelScope.launch { stateFlow.emit(UniversalIntroState.Initial(stateFlow.value.data)) }
    }

    fun helpClicked() {
        viewModelScope.launch { stateFlow.emit(UniversalIntroState.GoToHelp(stateFlow.value.data)) }
    }

    fun onBackPressed() {
        viewModelScope.launch { stateFlow.emit(UniversalIntroState.Cancel(stateFlow.value.data)) }
    }

    @VisibleForTesting
    fun secretTransfer(email: String) {
        flow {
            val transferId = requestTransfer(email).transferId
            val keyPair = sodiumCryptography.generateKeyExchangeKeyPair() ?: throw Exception()

            
            val senderPublicKey = startReceiverKeyExchange(publicKey = keyPair.publicKey, transferId = transferId)

            
            emit(UniversalIntroState.LoadingPassphrase(stateFlow.value.data))

            
            coroutineScope {
                launch { completeKeyExchange(keyPair.publicKey.encodeBase64ToString(), transferId) }
                launch { delay(500) }
            }

            val receiverSessionKeys = sodiumCryptography.keyExchangeClientSessionKeys(
                clientKeyPair = keyPair,
                serverPublicKey = senderPublicKey.decodeBase64ToByteArray()
            ) ?: run {
                throw IllegalStateException()
            }

            val passphrase = generatePassphrase(
                passphraseGenerator = passphraseGenerator,
                sodiumCryptography = sodiumCryptography,
                sessionKey = receiverSessionKeys.rx,
                login = email,
                transferId = transferId
            )

            secretTransferAnalytics.pageView(AnyPage.LOGIN_DEVICE_TRANSFER_SECURITY_CHALLENGE)
            emit(UniversalIntroState.PassphraseVerification(stateFlow.value.data.copy(passphrase = passphrase)))

            
            val startTransferData = startTransfer(transferId)

            
            emit(UniversalIntroState.LoadingAccount(stateFlow.value.data))

            val secretTransferPayload = decryptPayload(
                email = email,
                transferId = transferId,
                receiverSessionKeys = receiverSessionKeys,
                encryptedData = startTransferData.encryptedData,
                nonce = startTransferData.nonce
            )

            val registeredUserDevice = registrationWithAuthTicket(
                login = secretTransferPayload.login,
                token = secretTransferPayload.token ?: throw IllegalStateException(),
                securityFeatures = emptySet(),
                remoteKeyType = RemoteKey.Type.MASTER_PASSWORD
            )
            emit(UniversalIntroState.Success(stateFlow.value.data.copy(email = email), secretTransferPayload, registeredUserDevice))
        }
            .flowOn(ioDispatcher)
            .catch {
                val error = when (it) {
                    is DashlaneApiHttp504Exception -> UniversalIntroError.Timeout
                    else -> UniversalIntroError.Generic
                }
                emit(UniversalIntroState.Error(stateFlow.value.data.copy(email = email), error = error))
            }
            .onEach { state -> stateFlow.emit(state) }
            .launchIn(viewModelScope)
    }

    @VisibleForTesting
    suspend fun requestTransfer(email: String?): RequestTransferService.Data {
        val transfer = RequestTransferService.Request.Transfer(
            transferType = TransferType.UNIVERSAL,
            receiverDeviceName = deviceInfoRepository.deviceName,
            login = email
        )
        val request = RequestTransferService.Request(transfer)
        return requestTransferService.execute(request).data
    }

    @VisibleForTesting
    suspend fun startReceiverKeyExchange(publicKey: ByteArray, transferId: String): String {
        val publicKeyHash = sodiumCryptography.genericHash(publicKey).encodeBase64ToString()
        val request = StartReceiverKeyExchangeService.Request(transferId = transferId, receiverHashedPublicKey = publicKeyHash)
        val response = startReceiverKeyExchangeService.execute(request)
        return response.data.senderPublicKey
    }

    @VisibleForTesting
    suspend fun completeKeyExchange(receiverPublicKey: String, transferId: String) {
        val request = CompleteKeyExchangeService.Request(transferId = transferId, receiverPublicKey = receiverPublicKey)
        completeKeyExchangeService.execute(request)
    }

    @VisibleForTesting
    suspend fun startTransfer(transferId: String): StartTransferService.Data {
        val request = StartTransferService.Request(transferId = transferId, transferType = TransferType.UNIVERSAL)
        return startTransferService.execute(request).data
    }

    @VisibleForTesting
    fun decryptPayload(
        email: String,
        transferId: String,
        receiverSessionKeys: ClientKeyPair,
        encryptedData: String,
        nonce: String
    ): SecretTransferPayload {
        val message = "${SodiumCryptography.ENCRYPTION_KEY_HEADER}${email.length}$email$transferId".encodeUtf8ToByteArray()
        val receiverSecretKey = sodiumCryptography.genericHash(message, receiverSessionKeys.rx)
        val decryptedData = sodiumCryptography.secretboxOpenEasy(
            cipherText = encryptedData.decodeBase64ToByteArray(),
            nonce = nonce.decodeBase64ToByteArray(),
            key = receiverSecretKey
        )?.decodeUtf8ToString() ?: throw Exception()

        return moshi.adapter(SecretTransferPayload::class.java).fromJson(decryptedData) ?: throw Exception()
    }

    @VisibleForTesting
    suspend fun registrationWithAuthTicket(
        login: String,
        token: String,
        securityFeatures: Set<SecurityFeature>,
        remoteKeyType: RemoteKey.Type
    ): RegisteredUserDevice.Remote {
        userAccountStorage.saveSecuritySettings(username = login, securitySettings = UserSecuritySettings(isToken = true))
        return authenticationSecretTransferRepository.register(
            login = login,
            securityFeatures = securityFeatures,
            token = token,
            remoteKeyType = remoteKeyType
        )
    }
}
