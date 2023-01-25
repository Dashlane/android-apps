package com.dashlane.dagger.singleton

import com.dashlane.nitro.api.tools.CloudflareNitroHeaderInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.Call
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@InstallIn(SingletonComponent::class)
@Module
object NitroModule {

    @Singleton
    @Provides
    @Named("NitroCallFactory")
    fun provideNitroCallFactory(): Call.Factory = OkHttpClient.Builder()
        .cookieJar(MemoryCookieJar())
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(CloudflareNitroHeaderInterceptor())
        .build()
}

class MemoryCookieJar : CookieJar {
    private val cache = mutableSetOf<WrappedCookie>()

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesToRemove = mutableSetOf<WrappedCookie>()
        val validCookies = mutableSetOf<WrappedCookie>()

        cache.forEach { cookie ->
            if (cookie.isExpired()) {
                cookiesToRemove.add(cookie)
            } else if (cookie.matches(url)) {
                validCookies.add(cookie)
            }
        }

        cache.removeAll(cookiesToRemove)

        return validCookies.toList().map(WrappedCookie::unwrap)
    }

    @Synchronized
    fun saveFromHeader(url: String, setCookieHeader: String) {
        val cookie = Cookie.parse(url.toHttpUrl(), setCookieHeader) ?: return
        val wrappedCookie = WrappedCookie.wrap(cookie)

        cache.remove(wrappedCookie)
        cache.add(wrappedCookie)
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookiesToAdd = cookies.map { WrappedCookie.wrap(it) }

        cache.removeAll(cookiesToAdd)
        cache.addAll(cookiesToAdd)
    }

    @Synchronized
    fun clear() {
        cache.clear()
    }
}

class WrappedCookie private constructor(val cookie: Cookie) {
    fun unwrap() = cookie

    fun isExpired() = cookie.expiresAt < System.currentTimeMillis()

    fun matches(url: HttpUrl) = cookie.matches(url)

    override fun equals(other: Any?): Boolean {
        if (other !is WrappedCookie) return false

        return other.cookie.name == cookie.name &&
                other.cookie.domain == cookie.domain &&
                other.cookie.path == cookie.path &&
                other.cookie.secure == cookie.secure &&
                other.cookie.hostOnly == cookie.hostOnly
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = 31 * hash + cookie.name.hashCode()
        hash = 31 * hash + cookie.domain.hashCode()
        hash = 31 * hash + cookie.path.hashCode()
        hash = 31 * hash + if (cookie.secure) 0 else 1
        hash = 31 * hash + if (cookie.hostOnly) 0 else 1
        return hash
    }

    companion object {
        fun wrap(cookie: Cookie) = WrappedCookie(cookie)
    }
}