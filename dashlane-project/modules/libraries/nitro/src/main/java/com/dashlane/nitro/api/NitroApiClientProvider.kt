package com.dashlane.nitro.api

import com.dashlane.nitro.cryptography.NitroSecretStreamClient
import com.dashlane.nitro.cryptography.SecretStreamStates
import com.dashlane.server.api.DashlaneApiClient
import com.dashlane.server.api.NitroApiClient
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

internal class NitroApiClientProvider @Inject constructor(
    @DefaultCoroutineDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) {
    fun getNitroApiClient(
        tunnelUid: String?,
        apiClient: DashlaneApiClient,
        nitroSecretStreamClient: NitroSecretStreamClient,
        secretStreamStates: SecretStreamStates,
    ): NitroApiClient {
        return if (tunnelUid != null) {
            NitroApiProxyClientImpl(
                apiClient = apiClient,
                nitroSecretStreamClient = nitroSecretStreamClient,
                secretStreamStates = secretStreamStates,
                coroutineDispatcher = coroutineDispatcher,
                tunnelId = tunnelUid,
            )
        } else {
            NitroApiClientImpl(
                apiClient = apiClient,
                nitroSecretStreamClient = nitroSecretStreamClient,
                secretStreamStates = secretStreamStates,
                coroutineDispatcher = coroutineDispatcher,
            )
        }
    }
}