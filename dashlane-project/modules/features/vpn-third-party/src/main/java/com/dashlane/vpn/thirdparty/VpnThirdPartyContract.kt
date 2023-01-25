package com.dashlane.vpn.thirdparty

import android.content.Intent
import com.skocken.presentation.definition.Base

interface VpnThirdPartyContract {

    interface Presenter : Base.IPresenter {
        fun onStart()
        fun onResume()

        fun onActivateClicked()
        fun onInstallAppClicked()
        fun onLaunchAppClicked()
        fun onLearnMoreClicked()
        fun onDismissClicked()
        fun onQuestionOneReadMoreClicked()
        fun onCopyLoginClicked()
        fun onCopyPasswordClicked()
    }

    interface DataProvider : Base.IDataProvider {
        val isHotspotShieldInstalled: Boolean
        val installIntent: Intent
        val signInIntent: Intent

        

        suspend fun getHotspotShieldAccount(): Account?

        data class Account(val login: String, val password: String, val itemId: String, val domain: String)
    }

    interface ViewProxy : Base.IView {
        fun showInfobox()
        fun showActivate()
        fun showGettingStarted()
        fun showAccount(username: String, password: String)
        fun showLaunchAppButton()
    }
}