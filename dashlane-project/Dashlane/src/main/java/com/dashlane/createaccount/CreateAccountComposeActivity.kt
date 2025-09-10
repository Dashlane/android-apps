package com.dashlane.createaccount

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.createaccount.passwordless.MplessAccountCreationActivity
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterLink
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.disableAutoFill
import com.dashlane.util.launchUrl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateAccountComposeActivity : DashlaneActivity() {
    @Inject
    lateinit var createAccountSuccessIntentFactory: CreateAccountSuccessIntentFactory

    override var requireUserUnlock: Boolean = false

    private val createAccountViewModel: CreateAccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        this.disableAutoFill()

        setContent {
            DashlaneTheme {
                val uiState by createAccountViewModel.stateFlow.viewState.collectAsStateWithLifecycle()
                CreateAccountNavigation(
                    startDestination = uiState.startDestination,
                    goToPasswordless = ::goToPasswordless,
                    success = ::success,
                    onHelpCenter = ::onHelpCenter,
                )
            }
        }
    }

    private fun goToPasswordless(email: String) {
        startActivity(
            Intent(this, MplessAccountCreationActivity::class.java)
                .apply { putExtra(MplessAccountCreationActivity.EXTRA_USER_LOGIN, email) },
        )
    }

    private fun success() {
        startActivity(createAccountSuccessIntentFactory.createIntent())
        finishAffinity()
    }

    private fun onHelpCenter(uri: Uri?) {
        launchUrl(uri ?: HelpCenterLink.Base.androidUri)
    }

    companion object {
        const val EXTRA_PRE_FILLED_EMAIL = "pre_filled_email"
        const val EXTRA_SKIP_EMAIL_IF_PRE_FILLED = "skipEmailIfPrefilled"
    }
}