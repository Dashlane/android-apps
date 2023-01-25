package com.dashlane.guidedpasswordchange

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangeContract
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangeDataProvider
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangePresenter
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangeViewProxy
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.ToasterImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingGuidedPasswordChangeActivity : DashlaneActivity() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    private lateinit var presenter: OnboardingGuidedPasswordChangeContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_guided_password_change)
        actionBarUtil.setup()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.guided_password_toolbar_title)
        }
        itemUpdated = false
        val domain = intent?.getStringExtra(EXTRA_DOMAIN)!!
        val username = intent?.getStringExtra(EXTRA_USERNAME)
        val itemId = intent?.getStringExtra(EXTRA_ITEM_ID)!!
        val hasInlineAutofill =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && userPreferencesManager.hasInlineAutofill
        val viewProxy = OnboardingGuidedPasswordChangeViewProxy(this, domain, hasInlineAutofill)
        presenter = OnboardingGuidedPasswordChangePresenter(
            domain,
            username,
            itemId,
            lifecycleScope,
            navigator,
            ToasterImpl(this),
            lockHelper
        ).apply {
            setProvider(OnboardingGuidedPasswordChangeDataProvider())
            setView(viewProxy)
            this.onCreate(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
        itemUpdated = false
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    companion object {
        private const val EXTRA_DOMAIN = "website_domain"
        private const val EXTRA_ITEM_ID = "item_id"
        private const val EXTRA_USERNAME = "username"
        var currentDomainUsername: Pair<String, String?>? = null
        var itemUpdated = false
    }
}