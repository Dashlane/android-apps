package com.dashlane.login.sso.migration

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MigrationToSsoMemberActivity : DashlaneActivity() {
    override var requireUserUnlock = false

    @Inject
    lateinit var presenter: MigrationToSsoMemberContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val login = intent.getStringExtra(KEY_LOGIN)
        val serviceProviderUrl = intent.getStringExtra(KEY_SERVICE_PROVIDER_URL)
        val isNitroProvider = intent.getBooleanExtra(KEY_IS_NITRO_PROVIDER, false)

        if (login == null || serviceProviderUrl == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_migration_to_sso_member)

        val viewProxy = MigrationToSsoMemberViewProxy(this)
        presenter.setView(viewProxy)
        presenter.onCreate(savedInstanceState, login, serviceProviderUrl, isNitroProvider)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        
    }

    companion object {
        private const val KEY_LOGIN = "login"
        private const val KEY_SERVICE_PROVIDER_URL = "service_provider_url"
        private const val KEY_IS_NITRO_PROVIDER = "is_nitro_provider"
        const val KEY_TOTP_AUTH_TICKET = "totp_auth_ticket"

        fun newIntent(
            context: Context,
            login: String,
            serviceProviderUrl: String,
            isNitroProvider: Boolean,
            totpAuthTicket: String?
        ): Intent = Intent(context, MigrationToSsoMemberActivity::class.java)
            .putExtra(KEY_LOGIN, login)
            .putExtra(KEY_SERVICE_PROVIDER_URL, serviceProviderUrl)
            .putExtra(KEY_IS_NITRO_PROVIDER, isNitroProvider)
            .putExtra(KEY_TOTP_AUTH_TICKET, totpAuthTicket)
    }
}