package com.dashlane.nitro.api.tools

import com.dashlane.nitro.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response



class CloudflareNitroHeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return if (BuildConfig.CLOUDFLARE_NITRO_CLIENTID.isEmpty()) {
            chain.proceed(chain.request())
        } else {
            val request = chain.request().newBuilder()
                .header("CF-Access-Client-Id", BuildConfig.CLOUDFLARE_NITRO_CLIENTID)
                .header("CF-Access-Client-Secret", BuildConfig.CLOUDFLARE_NITRO_SECRET)
                .build()
            chain.proceed(request)
        }
    }
}