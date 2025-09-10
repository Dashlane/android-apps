package com.dashlane.nitro

import com.dashlane.nitro.CoseSign1.Companion.decodeCoseSign1OrNull
import com.dashlane.nitro.api.DashlaneApiProvider
import com.dashlane.nitro.api.NitroApiClientProvider
import com.dashlane.nitro.cryptography.NitroSecretStreamClient
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.nitro.util.encodeHex
import com.dashlane.server.api.NitroApi
import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.tunnel.ClientHelloService
import com.dashlane.server.api.endpoints.tunnel.ClientHelloService.Request.ClientPublicKey
import com.dashlane.server.api.endpoints.tunnel.TerminateHelloService
import com.dashlane.server.api.endpoints.tunnel.TerminateHelloService.Request.ClientHeader
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException
import dagger.Reusable
import okio.ByteString.Companion.decodeHex
import javax.inject.Inject

@Reusable
internal class NitroImpl @Inject constructor(
    private val dashlaneApiProvider: DashlaneApiProvider,
    private val nitroApiClientProvider: NitroApiClientProvider,
    private val nitroSecretStreamClient: NitroSecretStreamClient,
    private val sodiumCryptography: SodiumCryptography,
    private val attestationValidator: AttestationValidator,
    private var nitroHost: String,
    private var behindProxy: Boolean,
) : Nitro {
    private lateinit var nitroApi: NitroApi

    override suspend fun authenticate(newNitroHost: String) {
        nitroHost = newNitroHost
        return authenticate()
    }

    override suspend fun authenticate() {
        val dashlaneApiClient = dashlaneApiProvider.getApiClient(
            nitroHost = nitroHost,
            behindProxy = behindProxy,
        )
        val dashlaneApi = dashlaneApiProvider.getApi(
            dashlaneApiClient = dashlaneApiClient,
            behindProxy = behindProxy,
        )
        val nitroTunnelEndpoints = dashlaneApi.endpoints.tunnel

        val clientKeyPair = sodiumCryptography.generateKeyExchangeKeyPair()
            ?: throw NitroCryptographyException(message = "Failed to generate key pair")

        val clientHelloResponse = nitroTunnelEndpoints.clientHelloService.execute(
            request = ClientHelloService.Request(
                clientPublicKey = ClientPublicKey(clientKeyPair.publicKey.encodeHex()),
            ),
        )

        val coseSign1Attestation =
            clientHelloResponse.data.attestation.decodeHex().toByteArray()
                .decodeCoseSign1OrNull()
                ?: throw NitroException("Could not decode attestation in CoseSign1 format.")

        val serverInfo = attestationValidator.validate(coseSign1Attestation)

        val clientInfo = nitroSecretStreamClient.initializeSecretStream(
            clientKeyPair = clientKeyPair,
            serverInfo = serverInfo,
        )
        val tunnelUid = clientHelloResponse.data.tunnelUuid
        nitroTunnelEndpoints.terminateHelloService.execute(
            request = TerminateHelloService.Request(
                clientHeader = ClientHeader(clientInfo.header.encodeHex()),
                tunnelUuid = tunnelUid,
            ),
        )
        val nitroApiClient = nitroApiClientProvider.getNitroApiClient(
            tunnelUid = tunnelUid,
            apiClient = dashlaneApiClient,
            nitroSecretStreamClient = nitroSecretStreamClient,
            secretStreamStates = clientInfo.states,
        )
        nitroApi = NitroApi(dashlaneApi = dashlaneApi, client = nitroApiClient)
    }

    override suspend fun <T> execute(block: suspend NitroApi.() -> Response<T>): Response<T> {
        
        if (::nitroApi.isInitialized.not()) {
            authenticate()
        }
        
        return try {
            runNitroCall(block)
        } catch (e: DashlaneApiHttp400BusinessException) {
            
            handleNitroExceptions(e)
            
            runNitroCall(block)
        }
    }

    private suspend fun handleNitroExceptions(e: DashlaneApiHttp400BusinessException) = when (e) {
        
        
        is com.dashlane.server.api.endpoints.authentication.exceptions.TunnelUuidNotFoundException,
        is com.dashlane.server.api.endpoints.authentication.exceptions.ClientIdentifierNotFoundException,
        is com.dashlane.server.api.endpoints.authentication.exceptions.ClientSessionNotParsedException,
        is com.dashlane.server.api.endpoints.authentication.exceptions.ClientStateinNotFoundException,
        is com.dashlane.server.api.endpoints.logs.exceptions.TunnelUuidNotFoundException,
        is com.dashlane.server.api.endpoints.logs.exceptions.ClientIdentifierNotFoundException,
        is com.dashlane.server.api.endpoints.logs.exceptions.ClientSessionNotParsedException,
        is com.dashlane.server.api.endpoints.logs.exceptions.ClientStateinNotFoundException,
        is com.dashlane.server.api.endpoints.uvvs.exceptions.TunnelUuidNotFoundException,
        is com.dashlane.server.api.endpoints.uvvs.exceptions.ClientIdentifierNotFoundException,
        is com.dashlane.server.api.endpoints.uvvs.exceptions.ClientSessionNotParsedException,
        is com.dashlane.server.api.endpoints.uvvs.exceptions.ClientStateinNotFoundException,
        is com.dashlane.server.api.endpoints.passkeys.exceptions.TunnelUuidNotFoundException,
        is com.dashlane.server.api.endpoints.passkeys.exceptions.ClientIdentifierNotFoundException,
        is com.dashlane.server.api.endpoints.passkeys.exceptions.ClientSessionNotParsedException,
        is com.dashlane.server.api.endpoints.passkeys.exceptions.ClientStateinNotFoundException,
        
        is com.dashlane.server.api.endpoints.authentication.exceptions.SecureTunnelMustBeReopenedException,
        is com.dashlane.server.api.endpoints.logs.exceptions.SecureTunnelMustBeReopenedException,
        is com.dashlane.server.api.endpoints.uvvs.exceptions.SecureTunnelMustBeReopenedException,
        is com.dashlane.server.api.endpoints.passkeys.exceptions.SecureTunnelMustBeReopenedException,
        -> authenticate()
        
        else -> throw e
    }

    private suspend fun <T> runNitroCall(block: suspend NitroApi.() -> Response<T>): Response<T> =
        try {
            nitroApi.block()
        } catch (_: UninitializedPropertyAccessException) {
            
            throw Nitro.NitroInitializationException()
        }
}