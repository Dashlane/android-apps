package com.dashlane.network.tools

import com.dashlane.network.ServerUrlOverride
import okhttp3.Interceptor
import okhttp3.Response

class CloudflareHeaderInterceptor(private val serverUrlOverride: ServerUrlOverride) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val isStaging = serverUrlOverride.stagingEnabled
        return if (!isStaging) {
            chain.proceed(chain.request())
        } else {
            val request = chain.request().newBuilder()
                .header("CF-Access-Client-Id", serverUrlOverride.cloudFlareClientId)
                .header("CF-Access-Client-Secret", serverUrlOverride.cloudFlareSecret)
                .build()
            chain.proceed(request)
        }
    }
}