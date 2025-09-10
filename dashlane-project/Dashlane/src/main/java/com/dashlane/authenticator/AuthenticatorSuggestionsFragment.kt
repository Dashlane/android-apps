package com.dashlane.authenticator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dashlane.authenticator.suggestions.AuthenticatorBoardingResult
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsViewModel
import com.dashlane.authenticator.suggestions.ui.AuthenticatorSuggestionScreen
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.generated.definitions.AnyPage.TOOLS_AUTHENTICATOR_EXPLORE
import com.dashlane.hermes.generated.definitions.AnyPage.TOOLS_AUTHENTICATOR_WELCOME
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.launchUrl
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticatorSuggestionsFragment : AbstractContentFragment() {
    private val viewModel by viewModels<AuthenticatorSuggestionsViewModel>()

    private val args: AuthenticatorSuggestionsFragmentArgs by navArgs()

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            
            
            navigator.popBackStack()
            navigator.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(false)
        val hasOtpCredentials = args.hasSetupOtpCredentials
        val page =
            if (hasOtpCredentials) TOOLS_AUTHENTICATOR_EXPLORE else TOOLS_AUTHENTICATOR_WELCOME
        setCurrentPageView(page)
        val setupAuthenticatorResultLauncher = SetUpAuthenticatorResultContract()
            .register(
                coroutineScope = lifecycleScope,
                activityResultCaller = this,
                navigator = navigator,
                viewModel = viewModel,
            )

        val otpResultLauncher =
            registerForActivityResult(AuthenticatorBoardingResult()) { (id, otp) ->
                if (id != null && otp != null) viewModel.onOtpSetup(id, otp)
            }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    AuthenticatorSuggestionScreen(
                        viewModel = viewModel,
                        onAddLoginClick = { expandImportOptions ->
                            navigator.goToCredentialAddStep1(expandImportOptions = expandImportOptions)
                        },
                        onAdd2faTokenClick = {
                            viewModel.onSetupAuthenticator(
                                setupAuthenticatorResultLauncher,
                            )
                        },
                        goToLearnMoreAbout2FA = {
                            context.launchUrl(HelpCenterLink.ARTICLE_AUTHENTICATOR.androidUri)
                        },
                        goToHelpCenter = {
                            context.launchUrl("https://support.dashlane.com/hc/requests/new")
                        },
                        onSetupOtp = { item ->
                            viewModel.launchOrShowFrozenAccountPaywall {
                                otpResultLauncher.launch(item)
                            }
                        },
                        onSetupComplete = {
                            navigator.popBackStack()
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!args.hasSetupOtpCredentials) {
            activity?.onBackPressedDispatcher?.addCallback(backPressedCallback)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        activity.invalidateOptionsMenu()
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.remove()
    }
}