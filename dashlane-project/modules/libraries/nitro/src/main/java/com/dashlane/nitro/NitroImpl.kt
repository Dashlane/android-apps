package com.dashlane.nitro

import com.dashlane.network.NitroUrlOverride
import com.dashlane.nitro.CoseSign1.Companion.decodeCoseSign1OrNull
import com.dashlane.nitro.api.NitroApi
import com.dashlane.nitro.api.NitroApiClientImpl
import com.dashlane.nitro.api.NitroApiImpl
import com.dashlane.nitro.api.tools.toHostUrl
import com.dashlane.nitro.api.tunnel.NitroTunnelApiImpl
import com.dashlane.nitro.api.tunnel.endpoints.ClientHelloService
import com.dashlane.nitro.api.tunnel.endpoints.TerminateHelloService
import com.dashlane.nitro.cryptography.NitroSecretStreamClient
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.nitro.util.encodeHex
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.DashlaneApiClient
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.Call
import okio.ByteString.Companion.decodeHex
import javax.inject.Inject
import javax.inject.Named

@Reusable
internal class NitroImpl @Inject constructor(
    @Named("NitroCallFactory") private val callFactory: Call.Factory,
    private val connectivityCheck: ConnectivityCheck,
    private val nitroSecretStreamClient: NitroSecretStreamClient,
    private val sodiumCryptography: SodiumCryptography,
    nitroUrlOverride: NitroUrlOverride,
    private val attestationValidator: AttestationValidator = AttestationValidatorImpl(
        nitroUrlOverride
    ),
    @DefaultCoroutineDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : Nitro {
    override suspend fun authenticate(nitroUrl: String): NitroApi =
        withContext(coroutineDispatcher) {
            val apiClient = DashlaneApiClient(
                callFactory,
                connectivityCheck,
                nitroUrl.toHostUrl()
            )

            val nitroTunnelEndpoints = NitroTunnelApiImpl(apiClient).endpoints

            val clientKeyPair = sodiumCryptography.generateKeyExchangeKeyPair()
                ?: throw NitroCryptographyException(message = "Failed to generate key pair")

            val clientHelloResponse = nitroTunnelEndpoints.clientHelloService.execute(
                request = ClientHelloService.Request(
                    clientPublicKey = clientKeyPair.publicKey.encodeHex()
                )
            )

            val coseSign1Attestation =
                clientHelloResponse.data.attestation.decodeHex().toByteArray()
                    .decodeCoseSign1OrNull()
                    ?: throw NitroException("Could not decode attestation in CoseSign1 format.")

            val serverInfo = attestationValidator.validate(coseSign1Attestation)

            val clientInfo = nitroSecretStreamClient.initializeSecretStream(
                clientKeyPair,
                serverInfo
            )

            nitroTunnelEndpoints.terminateHelloService.execute(
                request = TerminateHelloService.Request(
                    clientHeader = clientInfo.header.encodeHex()
                )
            )

            NitroApiImpl(
                client = NitroApiClientImpl(
                    apiClient = apiClient,
                    nitroSecretStreamClient = nitroSecretStreamClient,
                    secretStreamStates = clientInfo.states,
                    coroutineDispatcher = coroutineDispatcher
                )
            )
        }
}