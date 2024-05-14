package com.dashlane.nitro.api

import com.dashlane.cryptography.decodeUtf8ToString
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.nitro.cryptography.NitroSecretStreamClient
import com.dashlane.nitro.cryptography.SecretStreamStates
import com.dashlane.nitro.util.encodeHex
import com.dashlane.server.api.DashlaneApiClient
import com.dashlane.server.api.Response
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException
import com.dashlane.server.api.exceptions.DashlaneApiJsonException
import com.dashlane.server.api.execute
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.decodeHex

internal class NitroApiClientImpl(
    private val apiClient: DashlaneApiClient,
    private val nitroSecretStreamClient: NitroSecretStreamClient,
    private val secretStreamStates: SecretStreamStates,
    private val coroutineDispatcher: CoroutineDispatcher
) : NitroApiClient {
    private val gson get() = apiClient.gson

    override suspend fun <ResponseDataT> execute(
        path: String,
        request: Any,
        responseDataClass: Class<ResponseDataT>,
        businessExceptions: Map<String, (String?, Throwable?, String, String?) -> DashlaneApiHttp400BusinessException>
    ): Response<ResponseDataT> = withContext(coroutineDispatcher) {
        val secretStreamRequestData = try {
            synchronized(secretStreamStates) {
                nitroSecretStreamClient.encryptSecretStream(
                    states = secretStreamStates,
                    message = gson.toJson(request).encodeUtf8ToByteArray()
                )
            }.encodeHex()
        } catch (e: Throwable) {
            throw DashlaneApiJsonException(
                message = "Failed to craft secret stream request",
                cause = e
            )
        }

        val secretStreamResponse: Response<String> = apiClient.execute(
            path = path,
            request = Request(data = secretStreamRequestData),
            businessExceptions = businessExceptions
        )

        val responseData = try {
            gson.fromJson(
                synchronized(secretStreamStates) {
                    nitroSecretStreamClient.decryptSecretStream(
                        states = secretStreamStates,
                        encrypted = secretStreamResponse.data.decodeHex().toByteArray()
                    )
                }.decodeUtf8ToString(),
                responseDataClass
            )
        } catch (e: Throwable) {
            throw DashlaneApiJsonException(
                message = "Failed to read secret stream response",
                cause = e
            )
        }

        Response(
            requestId = secretStreamResponse.requestId,
            data = responseData
        )
    }

    private data class Request(
        @SerializedName("data")
        val data: String
    )
}