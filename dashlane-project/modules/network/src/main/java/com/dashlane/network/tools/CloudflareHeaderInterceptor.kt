package com.dashlane.network.tools

import com.dashlane.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class CloudflareHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (BuildConfig.CLOUDFLARE_CLIENTID.isEmpty()) {
            chain.proceed(chain.request())
        } else {
            val request = chain.request().newBuilder()
                .header("CF-Access-Client-Id", BuildConfig.CLOUDFLARE_CLIENTID)
                .header("CF-Access-Client-Secret", BuildConfig.CLOUDFLARE_SECRET)
                .build()
            chain.proceed(request)
        }
    }
}