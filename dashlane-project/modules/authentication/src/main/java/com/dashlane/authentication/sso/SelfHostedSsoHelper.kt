package com.dashlane.authentication.sso

import android.content.Intent
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.authentication.sso.utils.toIdpUrl
import com.dashlane.authentication.sso.utils.toUserSsoInfo

object SelfHostedSsoHelper {

    fun getServiceProviderIntent(serviceProviderUrl: String, login: String): Intent? {
        return Intent(
            Intent.ACTION_VIEW,
            serviceProviderUrl.toIdpUrl(login)
        )
    }

    fun parseResult(login: String, intent: Intent): GetSsoInfoResult {
        val userSsoInfo = intent.data?.run {
            encodedFragment?.let {
                

                
                buildUpon()
                    .encodedQuery(it)
                    .encodedFragment(null)
                    .build()
            } ?: this
        }?.toUserSsoInfo()

        return if (userSsoInfo == null) {
            GetSsoInfoResult.Error.CannotOpenServiceProvider
        } else {
            GetSsoInfoResult.Success(login, userSsoInfo)
        }
    }
}