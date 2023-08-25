package com.dashlane.authenticator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.net.toUri
import com.dashlane.activatetotp.ActivateTotpAuthenticatorConnection
import com.dashlane.authenticator.ipc.AuthenticatorService
import com.dashlane.login.LoginInfo
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.server.api.endpoints.account.CreateAccountService
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionObserver
import com.dashlane.welcome.HasOtpsForBackupProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticatorAppConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: GlobalPreferencesManager,
    private val sessionManager: SessionManager
) : HasOtpsForBackupProvider, ActivateTotpAuthenticatorConnection {
    private val _otpsForBackup = MutableStateFlow(emptyList<Otp>())

    val otpsForBackup: List<Otp> get() = _otpsForBackup.value

    override val hasAuthenticatorInstalled get() = context.isAuthenticatorAppInstalled()

    private val serviceIntent
        get() = Intent().apply {
            component = ComponentName(
                "com.dashlane.authenticator",
                "com.dashlane.authenticator.ipc.PasswordManagerAuthenticatorService"
            )
        }

    val otpLogin
        get() = otpsForBackup.mapNotNull { it.user }
            .filter { CreateAccountService.Request.Login.regex.matches(it) }
            .toSet()
            .takeIf { it.size == 1 }
            ?.first()

    fun loadOtpsForBackup() {
        _otpsForBackup.value = emptyList()

        if (preferencesManager.getDefaultUsername() == null) {
            connect { _otpsForBackup.value = otpUrisForBackup.orEmpty().mapNotNull { UriParser.parse(it.toUri()) } }

            sessionManager.attach(object : SessionObserver {
                override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
                    
                    sessionManager.detach(this)
                    _otpsForBackup.value = emptyList()
                }
            })
        }
    }

    fun confirmBackupDone() {
        connect { confirmBackupDone() }
    }

    fun getOtpForBackupCountAsync(): Deferred<Int> = connectAsync { otpUrisForBackup.count() }

    override fun hasSaveDashlaneTokenAsync() = connectAsync {
        
        val versionParts = version.split(".")
        versionParts[0].toInt() >= 1 && versionParts[1].toInt() >= 1
    }

    override fun saveDashlaneTokenAsync(userId: String, otpUri: String) = connectAsync { saveDashlaneOtpUri(userId, otpUri) }

    override fun deleteDashlaneTokenAsync(userId: String) = connectAsync { deleteDashlaneOtpUri(userId) }

    private fun <T> connectAsync(block: AuthenticatorService.() -> T): Deferred<T> {
        val deferred = CompletableDeferred<T>()

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                try {
                    val result = AuthenticatorService.Stub.asInterface(requireNotNull(service)).block()
                    deferred.complete(result)
                } catch (e: Exception) {
                    deferred.completeExceptionally(e)
                } finally {
                    context.unbindService(this)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        try {
            val bound = context.bindService(
                serviceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )

            if (!bound) {
                deferred.completeExceptionally(ConnectionFailedException())
            }
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
        }

        return deferred
    }

    private fun connect(block: AuthenticatorService.() -> Unit) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                try {
                    service?.let { AuthenticatorService.Stub.asInterface(it).block() }
                } catch (_: Exception) {
                    
                } finally {
                    context.unbindService(this)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }

        runCatching {
            context.bindService(
                serviceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    class ConnectionFailedException : Exception("Failed to bind Authenticator app")
}