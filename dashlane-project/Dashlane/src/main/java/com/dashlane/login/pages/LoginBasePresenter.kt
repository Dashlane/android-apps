package com.dashlane.login.pages

import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.login.root.LoginPresenter
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates



abstract class LoginBasePresenter<P : LoginBaseContract.DataProvider, V : LoginBaseContract.View>(
    val rootPresenter: LoginPresenter,
    val coroutineScope: CoroutineScope
) : BasePresenter<P, V>(), LoginBaseContract.Presenter, CoroutineScope by coroutineScope {
    protected var savedInstanceState: Bundle? = null
    protected var isCreated = false
        private set

    

    override fun getViewOrNull(): V? {
        return super.getViewOrNull()
    }

    override var showProgress: Boolean
        get() = view.showProgress
        set(value) {
            viewOrNull?.showProgress = value
        }

    open var email: String? = null
        set(value) {
            field = value
            viewOrNull?.email = email
        }

    override fun onViewOrProviderChanged() {
        super.onViewOrProviderChanged()
        initView()
    }

    override var visible by Delegates.observable(false) { _, _, value -> if (value) view.requestFocus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        isCreated = true
        initView()
    }

    override fun onCleared() {
        super.onCleared()
        isCreated = false
    }

    open fun initView() {
        viewOrNull?.takeIf { isCreated }?.let { view ->
            view.init(savedInstanceState)
            if (email.isNullOrEmpty()) {
                email = savedInstanceState?.getString(STATE_EMAIL) ?: providerOrNull?.username
            } else {
                view.email = email
            }
        }
    }

    override fun onStart() {
        
    }

    override fun onNewIntent() {
        
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewOrNull?.onSaveInstanceState(outState)
        this.savedInstanceState = outState
    }

    final override fun onShow() = provider.onShow()

    override fun onBackPressed(): Boolean {
        provider.onBack()
        return false
    }

    final override fun notifyOffline() {
        rootPresenter.showProgress = false
        view.showError(R.string.offline)
    }

    override fun notifyNetworkError() {
        rootPresenter.showProgress = false
        view.showError(R.string.cannot_connect_to_server)
    }

    override fun notifyExpiredVersion() {
        rootPresenter.showProgress = false
        view.showError(R.string.expired_version_noupdate_title)
    }

    companion object {
        const val STATE_EMAIL = "login_email"
    }
}