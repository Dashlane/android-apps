package com.dashlane.masterpassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.login.lock.LockManager
import com.dashlane.masterpassword.logger.ChangeMasterPasswordLogger
import com.dashlane.navigation.Navigator
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeMasterPasswordActivity : DashlaneActivity() {
    @Inject
    lateinit var dataProvider: ChangeMasterPasswordContract.DataProvider

    @Inject
    lateinit var changeMasterPasswordFeatureAccessChecker: ChangeMasterPasswordFeatureAccessChecker

    @Inject
    lateinit var passwordStrengthEvaluator: PasswordStrengthEvaluator

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var changeMasterPasswordLogoutHelper: ChangeMasterPasswordLogoutHelper

    @Inject
    lateinit var logRepository: LogRepository

    @Inject
    lateinit var navigator: Navigator

    lateinit var presenter: ChangeMasterPasswordPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!presenter.onBackPressed()) {
            super.onBackPressed()
        }
    }

    private fun setupActivity(savedInstanceState: Bundle?) {
        val origin = intent.getParcelableExtraCompat<ChangeMasterPasswordOrigin>(EXTRA_ORIGIN)
        val warningDesktopShown = intent.getBooleanExtra(EXTRA_WARNING_DESKTOP_SHOWN, false)

        
        if (origin == null || !changeMasterPasswordFeatureAccessChecker.canAccessFeature(origin is ChangeMasterPasswordOrigin.Migration)) {
            finish()
            return
        }
        setContentView(R.layout.activity_change_master_password)
        val page = getPage(origin)
        if (page != null) {
            this.setCurrentPageView(page)
        }
        val viewProxy = ChangeMasterPasswordViewProxy(this, lockManager)
        val logger = ChangeMasterPasswordLogger(logRepository)
        presenter = ChangeMasterPasswordPresenter(
            passwordStrengthEvaluator,
            logger,
            origin,
            warningDesktopShown,
            changeMasterPasswordLogoutHelper,
            navigator,
            lifecycleScope
        )
        presenter.setProvider(dataProvider)
        presenter.setView(viewProxy)
        presenter.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        presenter.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val EXTRA_ORIGIN = "origin"
        private const val EXTRA_WARNING_DESKTOP_SHOWN = "warning_desktop_shown"

        fun newIntent(
            context: Context,
            origin: ChangeMasterPasswordOrigin?,
            warningDesktopShown: Boolean = false
        ): Intent {
            return Intent(context, ChangeMasterPasswordActivity::class.java)
                .putExtra(EXTRA_ORIGIN, origin)
                .putExtra(EXTRA_WARNING_DESKTOP_SHOWN, warningDesktopShown)
        }

        private fun getPage(origin: ChangeMasterPasswordOrigin): AnyPage? {
            return if (origin is ChangeMasterPasswordOrigin.Settings) {
                AnyPage.SETTINGS_SECURITY_CHANGE_MASTER_PASSWORD
            } else {
                null
            }
        }
    }
}