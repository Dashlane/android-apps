package com.dashlane.createaccount.passwordless

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.dashlane.createaccount.CreateAccountSuccessIntentFactory
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.endoflife.EndOfLifeObserver
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.Toaster
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.launchUrl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MplessAccountCreationActivity : DashlaneActivity() {

    companion object {
        const val EXTRA_USER_LOGIN = "user_login"
    }

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var endOfLifeObserver: EndOfLifeObserver

    @Inject
    lateinit var intentFactory: CreateAccountSuccessIntentFactory

    @Inject
    lateinit var biometricsAuthModule: BiometricAuthModule

    val viewModel: MplessAccountCreationViewModel by viewModels()

    override var requireUserUnlock: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val login = intent.getStringExtra(EXTRA_USER_LOGIN)

        
        if (login == null) {
            finish()
            return
        }
        viewModel.initLogin(login)
        setContent {
            DashlaneTheme {
                MplessAccountCreationNavigation(
                    viewModel = viewModel,
                    onCancel = ::onCancel,
                    onAccountCreated = ::onAccountCreated,
                    displayErrorMessage = ::displayErrorMessage,
                    displayExpirationErrorMessage = ::displayExpirationMessage,
                    onOpenHelpCenterPage = ::onOpenHelpCenterPage
                )
            }
        }
    }

    private fun onOpenHelpCenterPage(uri: Uri) {
        launchUrl(uri)
    }

    private fun onCancel() {
        finish()
    }

    private fun onAccountCreated() {
        startActivity(intentFactory.createIntent())
        finishAffinity()
    }

    private fun displayErrorMessage(errorId: Int) {
        toaster.show(errorId, Toast.LENGTH_SHORT)
    }

    private fun displayExpirationMessage() {
        endOfLifeObserver.showExpiredVersionMessaging(this)
    }
}
