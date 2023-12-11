package com.dashlane.createaccount

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.login.dagger.TrackingId
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.disableAutoFill
import com.dashlane.ui.endoflife.EndOfLife
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateAccountActivity : DashlaneActivity() {

    @Inject
    lateinit var dataProvider: CreateAccountContract.DataProvider

    @Inject
    lateinit var contactSsoAdministratorDialogFactory: ContactSsoAdministratorDialogFactory

    @Inject
    @TrackingId
    lateinit var trackingId: String

    @Inject
    lateinit var endOfLife: EndOfLife

    lateinit var presenter: CreateAccountPresenter

    override var requireUserUnlock: Boolean
        get() = false
        set(requireUserUnlock) {
            super.requireUserUnlock = requireUserUnlock
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.disableAutoFill()
        setContentView(R.layout.activity_login_create_account)
        val view = findViewById<View>(R.id.view_login_root)
        val viewProxy = CreateAccountViewProxy(view)

        presenter = CreateAccountPresenter(
            coroutineScope = this.lifecycleScope,
            preFilledEmail = intent.getStringExtra(EXTRA_PRE_FILLED_EMAIL),
            skipEmailIfPrefilled = intent.getBooleanExtra(EXTRA_SKIP_EMAIL_IF_PRE_FILLED, false),
            endOfLife = endOfLife,
            contactSsoAdministratorDialogFactory = contactSsoAdministratorDialogFactory
        ).apply {
            setView(viewProxy)
            setProvider(dataProvider)
            password = lastCustomNonConfigurationInstance as ObfuscatedByteArray?
            onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onBackPressed() {
        if (!presenter.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return presenter.password
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val EXTRA_PRE_FILLED_EMAIL = "pre_filled_email"
        const val EXTRA_SKIP_EMAIL_IF_PRE_FILLED = "skipEmailIfPrefilled"
    }
}