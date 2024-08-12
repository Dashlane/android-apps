package com.dashlane.ui.screens.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.dashlane.R
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.util.Toaster
import com.dashlane.util.launchUrl

class SettingPrivacySetting(
    private val context: Context,
    private val subscriptionCodeRepository: SubscriptionCodeRepository,
    private val toaster: Toaster
) {
    suspend fun open() {
        try {
            val subscriptionCode = subscriptionCodeRepository.get()
            val uri =
                Uri.parse("https://www.dashlane.com/privacy/settings/?subCode=$subscriptionCode")
            context.launchUrl(uri)
        } catch (t: Throwable) {
            toaster.show(R.string.network_error, Toast.LENGTH_SHORT)
        }
    }
}