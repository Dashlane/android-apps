package com.dashlane.vpn.thirdparty.activate

import com.dashlane.server.api.Response
import com.dashlane.server.api.endpoints.vpn.VpnGetCredentialsService
import com.dashlane.server.api.endpoints.vpn.exceptions.UserAlreadyHasAnAccountException
import com.dashlane.server.api.endpoints.vpn.exceptions.UserAlreadyHasAnAccountForProviderException
import com.dashlane.server.api.endpoints.vpn.exceptions.UserAlreadyHaveActiveVpnSubscriptionException
import com.dashlane.server.api.endpoints.vpn.exceptions.UserDoesntHaveVpnCapabilityException
import com.skocken.presentation.definition.Base

interface VpnThirdPartyActivateAccountContract {

    interface Presenter : Base.IPresenter {
        fun onLearnMoreClicked()
        fun isEmailValid(email: String): Boolean
        suspend fun onConfirmClicked(email: String)
        fun onContactProviderSupport()
        fun onContactSupport()
        suspend fun onTryAgain()
        fun onTryAgainAccountExists()
    }

    interface DataProvider : Base.IDataProvider {
        @Throws(
            UserAlreadyHasAnAccountException::class,
            UserAlreadyHasAnAccountForProviderException::class,
            UserAlreadyHaveActiveVpnSubscriptionException::class,
            UserDoesntHaveVpnCapabilityException::class
        )
        suspend fun createHotspotShieldAccount(email: String): Response<VpnGetCredentialsService.Data>
        suspend fun saveAccount(title: String, email: String, password: String)
    }

    interface ViewProxy : Base.IView
}