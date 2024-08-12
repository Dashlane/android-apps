package com.dashlane.login.controller

import android.app.Activity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.common.logger.developerinfo.DeveloperInfoLogger
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TokenChecker(
    private val loginTokensModule: LoginTokensModule,
    private val developerInfoLogger: DeveloperInfoLogger,
) {

    fun checkAndDisplayTokenIfNeeded(activity: DashlaneActivity, username: String) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            val pushToken = loginTokensModule.tokenHashmap[username]
            if (pushToken != null) {
                pushToken.clearNotification(activity)
                loginTokensModule.tokenHashmap.remove(username)

                val token = pushToken.token
                if (token != null) {
                    showTokenDialog(activity, token)
                } else {
                    developerInfoLogger.log(
                        action = "token_checker",
                        message = "can't display dialog because token is null"
                    )
                }
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