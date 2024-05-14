package com.dashlane.nitro.api.tools

import com.dashlane.network.NitroUrlOverride
import okhttp3.Interceptor
import okhttp3.Response

class CloudflareNitroHeaderInterceptor(private val nitroUrlOverride: NitroUrlOverride) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (!nitroUrlOverride.nitroStagingEnabled) {
            chain.proceed(chain.request())
        } else {
            val request = chain.request().newBuilder()
                .header("CF-Access-Client-Id", nitroUrlOverride.cloudFlareNitroClientId)
                .header("CF-Access-Client-Secret", nitroUrlOverride.cloudFlareNitroSecret)
                .build()
            chain.proceed(request)
        }
    }
}