package com.dashlane.login.controller

import android.app.Activity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.notification.model.TokenNotificationHandler
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.isNotSemanticallyNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TokenChecker(
    private val getTokenService: GetTokenService,
    private val tokenNotificationHandler: TokenNotificationHandler,
    private val loginTokensModule: LoginTokensModule
) {

    fun checkAndDisplayTokenIfNeeded(activity: DashlaneActivity, username: String, uki: String) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            retrieveToken(username, uki)?.let {
                val pushToken = loginTokensModule.tokenHashmap[username]
                if (pushToken != null) {
                    pushToken.setShown()
                    pushToken.clearNotification(activity)
                    loginTokensModule.tokenHashmap.remove(username)
                    loginTokensModule.tokenShouldNotifyHashmap[username] = false
                }
                showTokenDialog(activity, it)
            }
        }
    }

    private suspend fun retrieveToken(username: String, uki: String): String? = withContext(Dispatchers.Default) {
        
        val lastToken = tokenNotificationHandler.lastTokenForCurrentUser
        if (lastToken.isNotSemanticallyNull()) {
            tokenNotificationHandler.removeSavedToken()
            lastToken
        } else {
            val currentToken = loginTokensModule.tokenHashmap[username]
            
            if (currentToken == null || currentToken.needWebserviceCall()) {
                try {
                    getTokenService.execute(username, uki).content?.token
                } catch (e: IOException) {
                    null
                }
            } else {
                currentToken.token
            }
        }
    }

    private fun showTokenDialog(activity: Activity, token: String) {
        var displayableToken = token
        val tokenLength = token.length
        if (tokenLength == 6 || tokenLength == 8) {
            displayableToken = token.substring(0, tokenLength / 2) + " " + token.substring(tokenLength / 2)
        }

        val view = LayoutInflater.from(activity).inflate(R.layout.popup_token_view, null).apply {
            findViewById<TextView>(R.id.popup_token_code).text = displayableToken
        }
        DialogHelper().builder(activity)
            .setTitle(activity.getString(R.string.gcmmessage))
            .setView(view)
            .setPositiveButton(activity.getString(R.string.ok)) { _, _ -> }
            .show()
    }
}