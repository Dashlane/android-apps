package com.dashlane.login.sso

import android.content.Intent
import android.os.Bundle
import com.dashlane.authentication.create.AccountCreator
import com.skocken.presentation.definition.Base

interface LoginSsoContract {
    interface ViewProxy : Base.IView {
        fun showLoading()
        fun showTerms()
    }

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?, login: String, serviceProviderUrl: String, isNitroProvider: Boolean)
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun onSaveInstanceState(outState: Bundle)
        fun onTermsAgreed(optInOffers: Boolean)
    }

    interface DataProvider : Base.IDataProvider {
        suspend fun login(
            login: String,
            ssoToken: String,
            serviceProviderKey: String
        ): Intent

        suspend fun createAccount(
            login: String,
            ssoToken: String,
            serviceProviderKey: String,
            termsState: AccountCreator.TermsState
        ): Intent
    }

    class CannotStartSessionException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
}