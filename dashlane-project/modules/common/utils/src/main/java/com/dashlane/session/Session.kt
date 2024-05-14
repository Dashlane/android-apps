package com.dashlane.session

import com.dashlane.cryptography.CryptographyKey
import java.io.Closeable
import java.time.Clock
import kotlin.math.absoluteValue

@Suppress("UseDataClass")
class Session(
    val username: Username,
    val accessKey: String,
    val secretKey: String,
    localKey: LocalKey,
    appKey: AppKey,
    remoteKey: VaultKey.RemoteKey? = null,
    val clock: Clock = Clock.systemDefaultZone(),
) {
    val userId: String
        get() = username.email

    val deviceId
        get() = accessKey

    val uki: String
        get() = if (secretKey.startsWith(accessKey)) secretKey else "$accessKey-$secretKey"

    private val _localKey: LocalKey = localKey.clone()
    val localKey: LocalKey
        get() = _localKey.clone()
    private val _remoteKey = remoteKey?.clone()
    val remoteKey: VaultKey.RemoteKey?
        get() = _remoteKey?.clone()

    @Suppress("IfThenToElvis")
    val vaultKey: VaultKey
        get() = if (_remoteKey == null) (_appKey as AppKey.Password).toVaultKey() else _remoteKey.clone()
    private val _appKey: AppKey = appKey.clone()
    val appKey: AppKey
        get() = _appKey.clone()
    val userKeys
        get() = UserKeys(appKey, vaultKey)

    val appKeyType: CryptographyKey.Type
        get() = _appKey.cryptographyKeyType

    val sessionId: String = "$userId-${clock.millis()}".hashCode().absoluteValue.toString()

    init {
        require(appKey is AppKey.Password || remoteKey != null) { "App key $appKey requires non-null remote key." }
    }

    constructor(
        userId: String,
        accessKey: String,
        secretKey: String,
        localKey: LocalKey,
        appKey: AppKey,
        remoteKey: VaultKey.RemoteKey? = null,
        clock: Clock = Clock.systemDefaultZone(),
    ) : this(
        Username.ofEmail(userId),
        accessKey,
        secretKey,
        localKey,
        appKey,
        remoteKey,
        clock,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Session) return false
        if (username != other.username) return false
        if (accessKey != other.accessKey) return false
        if (secretKey != other.secretKey) return false
        if (_localKey != other._localKey) return false
        if (_remoteKey != other._remoteKey) return false
        if (_appKey != other._appKey) return false
        if (sessionId != other.sessionId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + accessKey.hashCode()
        result = 31 * result + secretKey.hashCode()
        result = 31 * result + _localKey.hashCode()
        result = 31 * result + _remoteKey.hashCode()
        result = 31 * result + _appKey.hashCode()
        result = 31 * result + sessionId.hashCode()
        return result
    }

    data class UserKeys(
        val app: AppKey,
        val vault: VaultKey
    ) : Closeable {
        override fun close() {
            app.close()
            vault.close()
        }
    }
}