package com.dashlane.authenticator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dashlane.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsViewModel
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsViewProxy
import com.dashlane.hermes.generated.definitions.AnyPage.TOOLS_AUTHENTICATOR_EXPLORE
import com.dashlane.hermes.generated.definitions.AnyPage.TOOLS_AUTHENTICATOR_WELCOME
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject
    lateinit var navigator: Navigator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(false)
        val view = inflater.inflate(R.layout.fragment_authenticator_suggestions, container, false)
        val hasOtpCredentials = args.hasSetupOtpCredentials
        val page =
            if (hasOtpCredentials) TOOLS_AUTHENTICATOR_EXPLORE else TOOLS_AUTHENTICATOR_WELCOME
        setCurrentPageView(page)
        AuthenticatorSuggestionsViewProxy(
            this,
            navigator,
            view,
            viewModel,
            lifecycle,
            hasOtpCredentials
        )
        return view
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