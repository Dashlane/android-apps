package com.dashlane.network.tools

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class MoreDetailedExceptionRequestInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            return chain.proceed(request)
        } catch (ex: Exception) {
            throw OkHttpRequestException(request, ex)
        }
    }

    class OkHttpRequestException(message: String, cause: Throwable) : IOException(message, cause) {

        constructor(request: Request, cause: Throwable) : this(toMessage(request), cause)

        companion object {
            private fun toMessage(request: Request): String {
                val url = request.url
                return "Exception while calling [${request.method}] ${url.host}${url.encodedPath}"
            }
        }
    }
}