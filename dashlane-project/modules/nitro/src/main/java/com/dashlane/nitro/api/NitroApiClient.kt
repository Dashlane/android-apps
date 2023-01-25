package com.dashlane.nitro.api

import com.dashlane.server.api.Response
import com.dashlane.server.api.exceptions.DashlaneApiHttp400BusinessException

internal interface NitroApiClient {
    

    suspend fun <ResponseDataT> execute(
        path: String,
        request: Any,
        responseDataClass: Class<ResponseDataT>,
        businessExceptions: Map<String, (String?, Throwable?, String, String?) -> DashlaneApiHttp400BusinessException> = emptyMap()
    ): Response<ResponseDataT>
}

internal suspend inline fun <reified ResponseDataT> NitroApiClient.execute(
    path: String,
    request: Any,
    businessExceptions: Map<String, (String?, Throwable?, String, String?) -> DashlaneApiHttp400BusinessException> = emptyMap()
) = execute(
    path = path,
    request = request,
    responseDataClass = ResponseDataT::class.java,
    businessExceptions = businessExceptions
)