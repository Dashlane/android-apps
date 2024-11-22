package com.dashlane.guidedpasswordchange

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangeContract
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangeDataProvider
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangePresenter
import com.dashlane.guidedpasswordchange.internal.OnboardingGuidedPasswordChangeViewProxy
import com.dashlane.navigation.Navigator
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.ToasterImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingGuidedPasswordChangeActivity : DashlaneActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var navigator: Navigator

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

        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.view_coordinator_layout)
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            WindowInsetsCompat.CONSUMED
        }

        itemUpdated = false
        val domain = intent?.getStringExtra(EXTRA_DOMAIN)!!
        val username = intent?.getStringExtra(EXTRA_USERNAME)
        val itemId = intent?.getStringExtra(EXTRA_ITEM_ID)!!
        val hasInlineAutofill =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && preferencesManager[sessionManager.session?.username].hasInlineAutofill
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