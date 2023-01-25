package com.dashlane.authenticator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dashlane.R
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardEditState.EditLogins
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardViewModel
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardViewProxy
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.NavControllerUtils.TOP_LEVEL_DESTINATIONS
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.MenuContainer
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticatorDashboardFragment : AbstractContentFragment() {

    private val viewModel by viewModels<AuthenticatorDashboardViewModel>()
    private val args: AuthenticatorDashboardFragmentArgs by navArgs()

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            val activity = requireActivity()
            if (activity is MenuContainer) activity.disableMenuAccess(false)
            
            navigator.setupActionBar(TOP_LEVEL_DESTINATIONS)
            viewModel.onBackToViewMode()
        }
    }

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var clipboardCopy: ClipboardCopy

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_authenticator_dashboard, container, false)
        setCurrentPageView(AnyPage.TOOLS_AUTHENTICATOR_LOGINS)
        args.otpUri?.let {
            viewModel.otpUri = it
            arguments = AuthenticatorDashboardFragmentArgs(otpUri = null).toBundle()
        }
        AuthenticatorDashboardViewProxy(
            this,
            navigator,
            view,
            viewModel,
            backPressedCallback
        ) { clipboardCopy.copyToClipboard(this, sensitiveData = false, autoClear = true) }
        return view
    }

    override fun onResume() {
        super.onResume()
        activity?.onBackPressedDispatcher?.addCallback(backPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.remove()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && viewModel.editState.value == EditLogins) {
            backPressedCallback.handleOnBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        activity.invalidateOptionsMenu()
    }
}