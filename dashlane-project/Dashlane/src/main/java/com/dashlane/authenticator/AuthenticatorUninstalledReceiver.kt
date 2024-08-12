package com.dashlane.authenticator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.network.tools.authorization
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.authenticator.UnSetAuthenticatorService
import com.dashlane.server.api.endpoints.authenticator.exceptions.AuthenticatorDoesNotExistException
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthenticatorUninstalledReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    @Inject
    lateinit var unSetAuthenticatorService: UnSetAuthenticatorService

    @Inject
    @ApplicationCoroutineScope
    lateinit var coroutineScope: CoroutineScope

    companion object {
        internal suspend fun disableAuthenticator(
            userPreferencesManager: UserPreferencesManager,
            session: Session,
            unSetAuthenticatorService: UnSetAuthenticatorService
        ) {
            val prefs = userPreferencesManager.preferencesFor(session.username)
            prefs.authenticatorEnrolledBiometric = false
            if (prefs.registeredAuthenticatorPushId == null) {
                
                return
            }

            val disabled = try {
                unSetAuthenticatorService.execute(session.authorization)
                true
            } catch (_: AuthenticatorDoesNotExistException) {
                true
            } catch (_: DashlaneApiException) {
                false
            }

            if (disabled) prefs.registeredAuthenticatorPushId = null
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_PACKAGE_FULLY_REMOVED) return
        if (intent.data?.schemeSpecificPart != PACKAGE_NAME_AUTHENTICATOR_APP) return

        if (context?.isAuthenticatorAppInstalled() == true) {
            
            return
        }
        val session = sessionManager.session ?: return
        coroutineScope.launch { disableAuthenticator(userPreferencesManager, session, unSetAuthenticatorService) }
    }
}
