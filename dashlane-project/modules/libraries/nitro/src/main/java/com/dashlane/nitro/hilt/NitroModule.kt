package com.dashlane.nitro.hilt

import com.dashlane.network.NitroUrlOverride
import com.dashlane.network.ServerUrlOverride
import com.dashlane.network.webservices.DashlaneUrls.URL_API
import com.dashlane.nitro.AttestationValidator
import com.dashlane.nitro.AttestationValidatorImpl
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.NitroImpl
import com.dashlane.nitro.api.DashlaneApiProvider
import com.dashlane.nitro.api.NitroApiClientProvider
import com.dashlane.nitro.api.tools.CloudflareNitroHeaderInterceptor
import com.dashlane.nitro.api.tools.toHostUrl
import com.dashlane.nitro.cryptography.NitroSecretStreamClient
import com.dashlane.nitro.cryptography.NitroSecretStreamClientImpl
import com.dashlane.nitro.cryptography.sodium.SodiumCryptography
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Call
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Optional
import javax.inject.Named
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@InstallIn(SingletonComponent::class)
@Module
internal interface NitroModule {
    @BindsOptionalOf
    fun bindsOptionalNitroUrlOverride(): NitroUrlOverride

    @Binds
    fun bindNitroSecretStreamClient(impl: NitroSecretStreamClientImpl): NitroSecretStreamClient

    @Binds
    fun bindAttestationValidator(impl: AttestationValidatorImpl): AttestationValidator

    companion object {
        @Provides
        @NitroLegacy
        @Suppress(
            
            "kotlin:S107",
        )
        fun providesNitroLegacy(
            dashlaneApiProvider: DashlaneApiProvider,
            nitroApiClientProvider: NitroApiClientProvider,
            nitroSecretStreamClient: NitroSecretStreamClient,
            sodiumCryptography: SodiumCryptography,
            attestationValidator: AttestationValidator,
            nitroUrlOverride: Optional<NitroUrlOverride>,
        ): Nitro {
            return NitroImpl(
                dashlaneApiProvider = dashlaneApiProvider,
                nitroApiClientProvider = nitroApiClientProvider,
                nitroSecretStreamClient = nitroSecretStreamClient,
                sodiumCryptography = sodiumCryptography,
                attestationValidator = attestationValidator,
                behindProxy = false,
                
                nitroHost = (nitroUrlOverride.getOrNull()?.nitroUrl ?: URL_API).toHostUrl(),
            )
        }

        @Provides
        @NitroProxy
        @Suppress(
            
            "kotlin:S107",
        )
        fun providesNitroProxy(
            dashlaneApiProvider: DashlaneApiProvider,
            nitroApiClientProvider: NitroApiClientProvider,
            nitroSecretStreamClient: NitroSecretStreamClient,
            sodiumCryptography: SodiumCryptography,
            attestationValidator: AttestationValidator,
            serverUrlOverride: Optional<ServerUrlOverride>,
        ): Nitro {
            return NitroImpl(
                dashlaneApiProvider = dashlaneApiProvider,
                nitroApiClientProvider = nitroApiClientProvider,
                nitroSecretStreamClient = nitroSecretStreamClient,
                sodiumCryptography = sodiumCryptography,
                attestationValidator = attestationValidator,
                
                nitroHost = (serverUrlOverride.getOrNull()?.apiUrl ?: URL_API).toHostUrl(),
                behindProxy = true,
            )
        }

        @Singleton
        @Provides
        @Named("NitroCallFactory")
        fun provideNitroCallFactory(nitroUrlOverride: Optional<NitroUrlOverride>): Call.Factory =
            OkHttpClient.Builder()
                .cookieJar(MemoryCookieJar())
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .apply { nitroUrlOverride.getOrNull()?.let { addInterceptor(CloudflareNitroHeaderInterceptor(it)) } }
                .build()
    }
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