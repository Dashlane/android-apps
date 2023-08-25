package com.dashlane.login.sso

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class LoginSsoActivity : DashlaneActivity() {

    override var requireUserUnlock = false

    @Inject
    lateinit var presenter: LoginSsoContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val login = intent.getStringExtra(KEY_LOGIN)
        val serviceProviderUrl = intent.getStringExtra(KEY_SERVICE_PROVIDER_URL)
        val isNitroProvider = intent.getBooleanExtra(KEY_IS_SSO_PROVIDER, false)

        if (login == null || serviceProviderUrl == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_sso_login)

        val viewProxy = LoginSsoViewProxy(this)
        presenter.setView(viewProxy)
        presenter.onCreate(savedInstanceState, login, serviceProviderUrl, isNitroProvider)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    sealed class Result : Parcelable {
        @Parcelize
        object Success : Result()

        sealed class Error : Result() {
            @Parcelize
            object InvalidSso : Error()

            @Parcelize
            object Network : Error()

            @Parcelize
            object Offline : Error()

            @Parcelize
            object ExpiredVersion : Error()

            @Parcelize
            object Unknown : Error()
        }
    }

    companion object {
        const val KEY_LOGIN = "login"
        const val KEY_IS_SSO_PROVIDER = "is_sso_provider"
        const val KEY_SERVICE_PROVIDER_URL = "service_provider_url"
        const val KEY_MIGRATE_TO_MASTER_PASSWORD_USER = "migrate_to_master_password_user"

        const val KEY_RESULT = "result"
    }
}

@get:StringRes
val LoginSsoActivity.Result.Error.errorResId
    get() = when (this) {
        is LoginSsoActivity.Result.Error.InvalidSso -> R.string.sso_error_not_correct
        is LoginSsoActivity.Result.Error.Network -> R.string.offline
        is LoginSsoActivity.Result.Error.Offline -> R.string.cannot_connect_to_server
        is LoginSsoActivity.Result.Error.Unknown -> R.string.login_button_error
        is LoginSsoActivity.Result.Error.ExpiredVersion -> R.string.expired_version_noupdate_title
    }