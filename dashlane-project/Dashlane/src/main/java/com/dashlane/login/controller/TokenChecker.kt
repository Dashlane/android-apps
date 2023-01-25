package com.dashlane.login.controller

import android.app.Activity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.navigation.NavigationConstants
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.notification.model.TokenNotificationHandler
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode33
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.util.Constants
import com.dashlane.util.isNotSemanticallyNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException



class TokenChecker(private val getTokenService: GetTokenService) {

    fun checkAndDisplayTokenIfNeeded(activity: DashlaneActivity, username: String, uki: String) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            retrieveToken(username, uki)?.let {
                val pushToken = Constants.GCM.Token[username]
                if (pushToken != null) {
                    pushToken.setShown()
                    pushToken.clearNotification(activity)
                    Constants.GCM.Token.remove(username)
                    Constants.GCM.TokenShouldNotify[username] = false
                }
                showTokenDialog(activity, it)
            }
        }
    }

    private suspend fun retrieveToken(username: String, uki: String): String? = withContext(Dispatchers.Default) {
        
        val lastToken = TokenNotificationHandler.getLastTokenForCurrentUser()
        if (lastToken.isNotSemanticallyNull()) {
            TokenNotificationHandler.removeSavedToken()
            lastToken
        } else {
            val currentToken = Constants.GCM.Token[username]
            
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

        val intent = activity.intent
        val alreadyLoggedIn = if (intent != null && intent.getBooleanExtra(
                NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION,
                false
            )
        ) {
            intent.removeExtra(
                NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION
            )
            intent.removeExtra(
                NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_USER
            )
            intent.removeExtra(
                NavigationConstants
                    .USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN
            )
            intent.getBooleanExtra(
                NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN,
                false
            )
        } else {
            false
        }

        val view = LayoutInflater.from(activity).inflate(R.layout.popup_token_view, null).apply {
            findViewById<TextView>(R.id.popup_token_code).text = displayableToken
        }
        DialogHelper().builder(activity)
            .setTitle(activity.getString(R.string.gcmmessage))
            .setView(view)
            .setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
                val pushTokenFrom = if (alreadyLoggedIn) {
                    UsageLogConstant.Push_Token_From.ALREADY_LOGGED_IN
                } else {
                    UsageLogConstant.Push_Token_From.NEEDED_LOGIN
                }
                UserActivityComponent(activity).currentSessionUsageLogRepository
                    ?.enqueue(
                        UsageLogCode33(
                            sender = UsageLogConstant.LoginOrigin.FROM_MOBILE,
                            type = "Push_Token",
                            confirm = "ok",
                            from = pushTokenFrom.toString()
                        )
                    )
            }
            .show()
    }
}