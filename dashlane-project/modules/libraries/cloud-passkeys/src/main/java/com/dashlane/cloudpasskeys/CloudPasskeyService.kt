package com.dashlane.cloudpasskeys

import com.dashlane.nitro.Nitro
import com.dashlane.nitro.hilt.NitroProxy
import com.dashlane.server.api.endpoints.passkeys.DeletePasskeyService
import com.dashlane.server.api.endpoints.passkeys.RegisterPasskeyService
import com.dashlane.server.api.endpoints.passkeys.RegisterPasskeyService.Request.Request.Options.PubKeyCredParam
import com.dashlane.server.api.endpoints.passkeys.UsePasskeyService
import com.dashlane.server.api.endpoints.passkeys.UsePasskeyService.Request.EncryptionKey
import com.dashlane.server.api.endpoints.passkeys.UsePasskeyService.Request.Request.Options
import com.dashlane.server.api.endpoints.passkeys.exceptions.PasskeyNotFoundException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.vault.model.CloudPasskeyData
import javax.inject.Inject
import com.dashlane.server.api.endpoints.passkeys.RegisterPasskeyService.Request.Request as RegisterRequest
import com.dashlane.server.api.endpoints.passkeys.RegisterPasskeyService.Request.Request.Options as RegisterOptions

class CloudPasskeyService @Inject constructor(
    private val sessionManager: SessionManager,
    @NitroProxy private val nitro: Nitro,
) {
    private val session: Session?
        get() = sessionManager.session

    suspend fun usePasskey(
        relyingPartyId: String,
        b64ClientDataHash: String,
        cloudPasskey: CloudPasskeyData,
    ) = nitro.execute {
        val authorization = session?.authorization
        requireNotNull(authorization)

        endpoints.passkeys.usePasskeyService.execute(
            userAuthorization = authorization,
            request = UsePasskeyService.Request(
                request = UsePasskeyService.Request.Request(
                    clientDataHash = b64ClientDataHash,
                    origin = relyingPartyId,
                    options = Options(
                        extensions = Options.Extensions(credProps = true),
                        rpId = relyingPartyId,
                    ),
                    userVerificationDone = true,
                ),
                passkeyId = cloudPasskey.passkeyId,
                encryptionKey = EncryptionKey(
                    uuid = cloudPasskey.keyId,
                    key = cloudPasskey.key,
                ),
            ),
        )
    }.data

    suspend fun register(
        relyingPartyName: String,
        relyingPartyId: String,
        userName: String,
        userId: String,
        b64ClientDataHash: String,
        passkeyAlgorithmIdentifier: Long,
        attestation: String,
    ) = nitro.execute {
        val authorization = session?.authorization
        requireNotNull(authorization)

        endpoints.passkeys.registerPasskeyService.execute(
            userAuthorization = authorization,
            request = RegisterPasskeyService.Request(
                request = RegisterRequest(
                    
                    origin = relyingPartyId,
                    clientDataHash = b64ClientDataHash,
                    userVerificationDone = true,
                    options = RegisterOptions(
                        extensions = RegisterOptions.Extensions(credProps = true),
                        user = RegisterOptions.User(
                            displayName = userName,
                            name = userName,
                            id = userId,
                        ),
                        attestation = RegisterOptions.Attestation.entries.firstOrNull { it.key == attestation }
                            ?: RegisterOptions.Attestation.NONE,
                        rp = RegisterOptions.Rp(
                            name = relyingPartyName,
                            id = relyingPartyId,
                        ),
                        pubKeyCredParams = listOf(
                            PubKeyCredParam(
                                type = PubKeyCredParam.Type.PUBLIC_KEY,
                                alg = passkeyAlgorithmIdentifier.toDouble(),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }.data

    suspend fun delete(cloudPasskey: CloudPasskeyData): Boolean {
        return try {
            nitro.execute {
                val authorization = session?.authorization
                requireNotNull(authorization)

                endpoints.passkeys.deletePasskeyService.execute(
                    userAuthorization = authorization,
                    request = DeletePasskeyService.Request(
                        passkeyId = cloudPasskey.passkeyId,
                        encryptionKey = DeletePasskeyService.Request.EncryptionKey(
                            uuid = cloudPasskey.keyId,
                            key = cloudPasskey.key,
                        ),
                    ),
                )
            }
            true
        } catch (e: PasskeyNotFoundException) {
            
            true
        } catch (e: DashlaneApiException) {
            
            false
        }
    }
}