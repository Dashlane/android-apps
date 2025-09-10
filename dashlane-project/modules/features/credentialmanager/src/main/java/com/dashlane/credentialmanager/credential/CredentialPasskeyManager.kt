package com.dashlane.credentialmanager.credential

import androidx.annotation.RequiresApi
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.PublicKeyCredential
import androidx.credentials.provider.CallingAppInfo
import com.dashlane.cloudpasskeys.CloudPasskeyService
import com.dashlane.credentialmanager.CredentialManagerDAO
import com.dashlane.credentialmanager.CredentialManagerDAO.PasskeySaveRequest
import com.dashlane.credentialmanager.CredentialManagerLogger
import com.dashlane.credentialmanager.model.CloudPasskeyNetworkException
import com.dashlane.credentialmanager.model.DashlaneCredentialManagerException
import com.dashlane.credentialmanager.model.MissingArgumentException
import com.dashlane.credentialmanager.model.PasskeyPrivateKeyException
import com.dashlane.credentialmanager.model.PasskeyUnsupportedAlgorithmException
import com.dashlane.credentialmanager.model.appInfoToOrigin
import com.dashlane.credentialmanager.model.b64Decode
import com.dashlane.credentialmanager.model.b64Encode
import com.dashlane.credentialmanager.model.fido.FidoPublicKeyCredential
import com.dashlane.credentialmanager.model.fido.common.AuthenticatorAttachment
import com.dashlane.credentialmanager.model.fido.common.ClientExtensionResults
import com.dashlane.credentialmanager.model.fido.common.ClientJson
import com.dashlane.credentialmanager.model.fido.common.CredentialType
import com.dashlane.credentialmanager.model.fido.common.RequestType
import com.dashlane.credentialmanager.model.fido.common.toInternalClientExtensionResults
import com.dashlane.credentialmanager.model.fido.request.PasskeyCreationOptions
import com.dashlane.credentialmanager.model.fido.request.PasskeyRequestOptions
import com.dashlane.credentialmanager.model.fido.response.AuthenticatorAssertionResponse
import com.dashlane.credentialmanager.model.fido.response.AuthenticatorAttestationResponse
import com.dashlane.credentialmanager.model.getB64HashForRequest
import com.dashlane.hermes.generated.definitions.CeremonyStatus
import com.dashlane.hermes.generated.definitions.PasskeyAuthenticationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyRegistrationErrorType
import com.dashlane.passkeys.algorithm.PasskeyAlgorithm
import com.dashlane.passkeys.algorithm.PasskeyAlgorithm.Cloud
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException
import com.dashlane.storage.userdata.restriction.PasskeysRestrictionsChecker
import com.dashlane.util.stackTraceToSafeString
import com.dashlane.vault.model.CloudPasskeyData
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.time.Instant
import javax.inject.Inject

interface CredentialPasskeyManager {
    suspend fun createPasskey(
        request: CreatePublicKeyCredentialRequest,
        requestOptions: PasskeyCreationOptions,
        callingAppInfo: CallingAppInfo,
    ): CreatePublicKeyCredentialResponse

    suspend fun providePasskey(
        requestOptions: PasskeyRequestOptions,
        vaultItem: VaultItem<SyncObject.Passkey>,
        callingAppInfo: CallingAppInfo,
        clientDataHash: ByteArray?,
    ): PublicKeyCredential
}

@RequiresApi(34)
@Suppress("LargeClass")
class CredentialPasskeyManagerImpl @Inject constructor(
    private val databaseAccess: CredentialManagerDAO,
    private val credentialManagerLogger: CredentialManagerLogger,
    private val cloudPasskeyService: CloudPasskeyService,
    private val passkeysRestrictionsChecker: PasskeysRestrictionsChecker,
) : CredentialPasskeyManager {
    override suspend fun createPasskey(
        request: CreatePublicKeyCredentialRequest,
        requestOptions: PasskeyCreationOptions,
        callingAppInfo: CallingAppInfo,
    ): CreatePublicKeyCredentialResponse {
        val passkeyAlgorithm = requestOptions.pubKeyCredParams.firstNotNullOfOrNull {
            PasskeyAlgorithm.provider(it.alg) as? PasskeyAlgorithm.Local
        }
        if (passkeyAlgorithm == null) {
            logCreatePasskeyError(
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
                errorType = PasskeyRegistrationErrorType.NOT_SUPPORTED,
            )
            throw PasskeyUnsupportedAlgorithmException(
                "Unsupported algorithm(s) ${requestOptions.pubKeyCredParams.map { it.alg }.joinToString(", ")}",
            )
        }
        val clientJson = ClientJson(
            type = RequestType.CREATE,
            challenge = requestOptions.challenge,
            origin = appInfoToOrigin(callingAppInfo),
            androidPackageName = callingAppInfo.packageName,
        )
        val (saveRequest, credential) = if (passkeysRestrictionsChecker.canUseCloudPasskeys()) {
            registerCloudPasskey(
                request = request,
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
                passkeyAlgorithm = passkeyAlgorithm,
                clientJson = clientJson,
            )
        } else {
            registerLocalPasskey(
                request = request,
                requestOptions = requestOptions,
                passkeyAlgorithm = passkeyAlgorithm,
                clientJson = clientJson,
            )
        }
        
        savePasskeyIntoDatabase(
            saveRequest = saveRequest,
            credentialId = saveRequest.credentialId,
            requestOptions = requestOptions,
            callingAppInfo = callingAppInfo,
        )

        return CreatePublicKeyCredentialResponse(registrationResponseJson = Json.encodeToString(credential))
    }

    private suspend fun registerCloudPasskey(
        request: CreatePublicKeyCredentialRequest,
        requestOptions: PasskeyCreationOptions,
        callingAppInfo: CallingAppInfo,
        passkeyAlgorithm: PasskeyAlgorithm.Local,
        clientJson: ClientJson,
    ): Pair<PasskeySaveRequest.Cloud, FidoPublicKeyCredential> {
        val registerResult = cloudPasskeyService.register(
            relyingPartyName = requestOptions.rp.name,
            relyingPartyId = requestOptions.rp.id,
            userName = requestOptions.user.displayName,
            userId = requestOptions.user.id,
            b64ClientDataHash = getB64HashForRequest(
                type = RequestType.CREATE,
                challenge = requestOptions.challenge,
                callingAppInfo = callingAppInfo,
                clientDataHash = request.clientDataHash,
            ),
            passkeyAlgorithmIdentifier = passkeyAlgorithm.algorithmIdentifier,
            attestation = requestOptions.attestation,
        )

        val registerData = registerResult.credentialRegisterData
        return PasskeySaveRequest.Cloud(
            cloudPasskeyData = CloudPasskeyData(
                key = registerResult.encryptionKey.key,
                keyId = registerResult.encryptionKey.uuid,
                passkeyId = registerResult.passkeyId,
            ),
            credentialId = registerData.rawId,
        ) to FidoPublicKeyCredential(
            rawId = registerData.rawId,
            id = registerData.id,
            type = CredentialType.fromResponseType(registerData.type),
            authenticatorAttachment = AuthenticatorAttachment.fromStringOrDefault(registerData.authenticatorAttachment),
            response = AuthenticatorAttestationResponse.fromRegisterData(
                registerData = registerData,
                passkeyAlgorithm = passkeyAlgorithm,
                clientJson = clientJson,
                request = request,
            ),
            clientExtensionResults = registerData.clientExtensionResults.toInternalClientExtensionResults(),
        )
    }

    private fun registerLocalPasskey(
        request: CreatePublicKeyCredentialRequest,
        requestOptions: PasskeyCreationOptions,
        passkeyAlgorithm: PasskeyAlgorithm.Local,
        clientJson: ClientJson,
    ): Pair<PasskeySaveRequest.Local, FidoPublicKeyCredential> {
        
        val rawCredentialId = ByteArray(32)
        SecureRandom().nextBytes(rawCredentialId)
        val credentialId = b64Encode(rawCredentialId)

        
        val keyPair = passkeyAlgorithm.createKeyPair()
        return PasskeySaveRequest.Local(
            privateKey = keyPair.toDashlanePrivateKey(),
            credentialId = credentialId,
        ) to FidoPublicKeyCredential(
            rawId = credentialId,
            id = credentialId,
            response = AuthenticatorAttestationResponse.fromLocalPasskey(
                clientDataHash = request.clientDataHash,
                publicKey = keyPair.public,
                publicKeyAlgorithm = passkeyAlgorithm.algorithmIdentifier,
                requestOptions = requestOptions,
                rawCredentialId = rawCredentialId,
                credentialPublicKey = passkeyAlgorithm.encodePublicKey(keyPair.public as ECPublicKey),
                clientJson = clientJson,
            ),
            type = CredentialType.fromTypeStringOrDefault(request.type),
            authenticatorAttachment = requestOptions.authenticatorSelection.authenticatorAttachment,
        )
    }

    override suspend fun providePasskey(
        requestOptions: PasskeyRequestOptions,
        vaultItem: VaultItem<SyncObject.Passkey>,
        callingAppInfo: CallingAppInfo,
        clientDataHash: ByteArray?,
    ): PublicKeyCredential {
        val syncObject = vaultItem.syncObject

        
        val counter = syncObject.counter ?: 0
        val counterHasPastIncrements = counter > 0
        val newCounter = if (counterHasPastIncrements) counter + 1 else 0

        
        databaseAccess.updatePasskey(vaultItem.uid, Instant.now(), newCounter)

        val passkeyAlgorithm = syncObject.keyAlgorithm?.let { PasskeyAlgorithm.provider(it) }
        val clientJson = ClientJson(
            type = RequestType.GET,
            challenge = requestOptions.challenge,
            origin = appInfoToOrigin(callingAppInfo),
            androidPackageName = callingAppInfo.packageName,
        )
        val (signature, clientExtensionResult) = when (passkeyAlgorithm) {
            is PasskeyAlgorithm.Local -> handleLocalPasskey(
                syncObject = syncObject,
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
                passkeyAlgorithm = passkeyAlgorithm,
                clientDataHash = clientDataHash,
                clientJson = clientJson,
            )
            is Cloud -> handleCloudPasskey(
                syncObject = syncObject,
                clientDataHash = clientDataHash,
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
            )
            null -> throwUnsupportedAlgorithm(
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
                vaultItem = vaultItem,
                syncObject = syncObject,
            )
        }

        val response = AuthenticatorAssertionResponse(
            b64ClientDataJson = if (clientDataHash == null) {
                b64Encode(Json.encodeToString(clientJson).toByteArray())
            } else {
                null
            },
            authenticatorData = b64Encode(requestOptions.getDefaultAuthenticatorData()),
            signature = b64Encode(signature),
            userHandle = syncObject.userHandle!!,
        )

        
        logPasskeyLoginSuccess(vaultItem, requestOptions, callingAppInfo)

        
        return PublicKeyCredential(
            authenticationResponseJson = Json.encodeToString(
                FidoPublicKeyCredential(
                    rawId = syncObject.credentialId!!,
                    id = syncObject.credentialId!!,
                    response = response,
                    type = CredentialType.PUBLIC_KEY,
                    authenticatorAttachment = AuthenticatorAttachment.PLATFORM,
                    clientExtensionResults = clientExtensionResult ?: ClientExtensionResults(),
                ),
            ),
        )
    }

    private fun throwUnsupportedAlgorithm(
        requestOptions: PasskeyRequestOptions,
        callingAppInfo: CallingAppInfo,
        vaultItem: VaultItem<SyncObject.Passkey>,
        syncObject: SyncObject.Passkey,
    ): Nothing {
        credentialManagerLogger.logPasskeyLogin(
            requestOptions = requestOptions,
            callingAppInfo = callingAppInfo,
            status = CeremonyStatus.FAILURE,
            vaultItem = vaultItem,
            errorType = PasskeyAuthenticationErrorType.NOT_SUPPORTED,
        )
        throw PasskeyUnsupportedAlgorithmException("Unsupported algorithms => ${syncObject.keyAlgorithm}")
    }

    private suspend fun handleCloudPasskey(
        syncObject: SyncObject.Passkey,
        clientDataHash: ByteArray?,
        requestOptions: PasskeyRequestOptions,
        callingAppInfo: CallingAppInfo,
    ) = try {
        val cloudPasskeyData = CloudPasskeyData.fromPasskey(passkey = syncObject)

        requireNotNull(cloudPasskeyData)
        val data = cloudPasskeyService.usePasskey(
            relyingPartyId = requestOptions.rpId,
            b64ClientDataHash = getB64HashForRequest(
                type = RequestType.GET,
                challenge = requestOptions.challenge,
                callingAppInfo = callingAppInfo,
                clientDataHash = clientDataHash,
            ),
            cloudPasskey = cloudPasskeyData,
        )
        
        b64Decode(data.credentialGetData.response.signature) to
            data.credentialGetData.clientExtensionResults.toInternalClientExtensionResults()
    } catch (e: IllegalArgumentException) {
        throw MissingArgumentException("Missing argument for remote validation: ${e.stackTraceToSafeString()}")
    } catch (e: DashlaneApiHttp400BusinessException) {
        throw CloudPasskeyNetworkException(e.stackTraceToSafeString())
    }

    private fun handleLocalPasskey(
        syncObject: SyncObject.Passkey,
        requestOptions: PasskeyRequestOptions,
        callingAppInfo: CallingAppInfo,
        passkeyAlgorithm: PasskeyAlgorithm.Local,
        clientJson: ClientJson,
        clientDataHash: ByteArray?,
    ): Pair<ByteArray, ClientExtensionResults?> {
        
        val privateKey = syncObject.privateKey
        if (privateKey == null) {
            credentialManagerLogger.logPasskeyLogin(
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
                status = CeremonyStatus.FAILURE,
                vaultItem = null,
                errorType = PasskeyAuthenticationErrorType.UNKNOWN,
            )
            throw PasskeyPrivateKeyException()
        }

        
        val hash = clientDataHash ?: MessageDigest.getInstance("SHA-256").digest(Json.encodeToString(clientJson).toByteArray())
        val dataToSign = requestOptions.getDefaultAuthenticatorData() + hash

        return passkeyAlgorithm.signChallenge(
            challenge = dataToSign,
            privateKey = privateKey.toJavaPrivateKey(),
        ) to null
    }

    private suspend fun savePasskeyIntoDatabase(
        saveRequest: PasskeySaveRequest,
        credentialId: String,
        requestOptions: PasskeyCreationOptions,
        callingAppInfo: CallingAppInfo,
    ): VaultItem<SyncObject.Passkey> {
        val savedPasskey = databaseAccess.savePasskeyCredential(
            saveRequest = saveRequest,
            credentialId = credentialId,
            rpName = requestOptions.rp.name,
            rpId = requestOptions.rp.id,
            userHandle = requestOptions.user.id,
            userDisplayName = requestOptions.user.displayName,
        )
        if (savedPasskey == null) {
            logCreatePasskeyError(
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
                errorType = PasskeyRegistrationErrorType.UNKNOWN,
            )
            throw DashlaneCredentialManagerException("Failed to save the passkey item")
        }

        
        credentialManagerLogger.logPasskeyCreate(
            requestOptions = requestOptions,
            callingAppInfo = callingAppInfo,
            vaultItem = savedPasskey,
            status = CeremonyStatus.SUCCESS,
        )

        return savedPasskey
    }

    private fun logPasskeyLoginSuccess(
        vaultItem: VaultItem<SyncObject.Passkey>,
        requestOptions: PasskeyRequestOptions,
        callingAppInfo: CallingAppInfo,
    ) {
        credentialManagerLogger.apply {
            activityLogPasskeyLogin(vaultItem = vaultItem, currentDomain = requestOptions.rpId)
            logPasskeyLogin(
                requestOptions = requestOptions,
                callingAppInfo = callingAppInfo,
                status = CeremonyStatus.SUCCESS,
                vaultItem = vaultItem,
                errorType = null,
            )
        }
    }

    private fun logCreatePasskeyError(
        requestOptions: PasskeyCreationOptions,
        callingAppInfo: CallingAppInfo,
        errorType: PasskeyRegistrationErrorType,
    ) {
        credentialManagerLogger.logPasskeyCreate(requestOptions, callingAppInfo, null, CeremonyStatus.FAILURE, errorType)
    }
}