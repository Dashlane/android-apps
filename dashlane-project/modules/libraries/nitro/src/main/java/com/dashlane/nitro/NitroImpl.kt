package com.dashlane.nitro

import com.dashlane.network.inject.HttpModule.Companion.SystemClockElapsedRealTime
import com.dashlane.nitro.CoseSign1.Companion.decodeCoseSign1OrNull
import com.dashlane.nitro.api.NitroApiClientImpl
import com.dashlane.nitro.api.NitroApiProxyClientImpl
import com.dashlane.nitro.api.tools.toHostUrl
import com.dashlane.nitro.cryptography.NitroSecretStreamClient
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import com.dashlane.nitro.util.encodeHex
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.DashlaneApi
import com.dashlane.server.api.DashlaneApiClient
import com.dashlane.server.api.NitroApi
import com.dashlane.server.api.endpoints.tunnel.ClientHelloService
import com.dashlane.server.api.endpoints.tunnel.ClientHelloService.Request.ClientPublicKey
import com.dashlane.server.api.endpoints.tunnel.TerminateHelloService
import com.dashlane.server.api.endpoints.tunnel.TerminateHelloService.Request.ClientHeader
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
    private val attestationValidator: AttestationValidator,
    private val authorization: Authorization.App,
    @DefaultCoroutineDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : Nitro {
    override suspend fun authenticate(nitroUrl: String, behindProxy: Boolean) =
        withContext(coroutineDispatcher) {
            val apiClient = DashlaneApiClient(
                callFactory,
                connectivityCheck,
                if (behindProxy) nitroUrl else "${nitroUrl.toHostUrl()}/api/"
            )
            val api = DashlaneApi(
                apiClient,
                authorization,
                SystemClockElapsedRealTime(),
                needSynchronizedClock = behindProxy
            )
            val nitroTunnelEndpoints = api.endpoints.tunnel

            val clientKeyPair = sodiumCryptography.generateKeyExchangeKeyPair()
                ?: throw NitroCryptographyException(message = "Failed to generate key pair")

            val clientHelloResponse = nitroTunnelEndpoints.clientHelloService.execute(
                request = ClientHelloService.Request(
                    clientPublicKey = ClientPublicKey(clientKeyPair.publicKey.encodeHex())
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
            val tunnelUid = clientHelloResponse.data.tunnelUuid
            nitroTunnelEndpoints.terminateHelloService.execute(
                request = TerminateHelloService.Request(
                    clientHeader = ClientHeader(clientInfo.header.encodeHex()),
                    tunnelUuid = tunnelUid
                )
            )
            val nitroApiClient = if (tunnelUid != null) {
                NitroApiProxyClientImpl(
                    apiClient = apiClient,
                    nitroSecretStreamClient = nitroSecretStreamClient,
                    secretStreamStates = clientInfo.states,
                    coroutineDispatcher = coroutineDispatcher,
                    tunnelId = tunnelUid
                )
            } else {
                NitroApiClientImpl(
                    apiClient = apiClient,
                    nitroSecretStreamClient = nitroSecretStreamClient,
                    secretStreamStates = clientInfo.states,
                    coroutineDispatcher = coroutineDispatcher
                )
            }
            NitroApi(api, nitroApiClient)
        }
}