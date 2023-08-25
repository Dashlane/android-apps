package com.dashlane.authenticator.dashboard

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.Companion.DEFAULT_ITEMS_SHOWN
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.R
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardCredentialItemAdapter.Listener
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HandleUri
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HasLogins
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HasLogins.CredentialItem
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.NoOtp
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.Progress
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract
import com.dashlane.authenticator.util.showAuthenticatorRemoveConfirmDialog
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.MenuContainer
import com.dashlane.ui.util.DialogHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthenticatorDashboardViewProxy(
    fragment: Fragment,
    navigator: Navigator,
    view: View,
    private val viewModel: AuthenticatorDashboardViewModelContract,
    private val editBackCallback: OnBackPressedCallback,
    private val authenticatorLogger: AuthenticatorLogger,
    copyCallback: String.() -> Unit
) {
    private val title = view.findViewById<TextView>(R.id.authenticator_dashboard_title)
    private val listContainer =
        view.findViewById<LinearLayout>(R.id.authenticator_dashboard_container)
    private val recyclerView =
        listContainer.findViewById<RecyclerView>(R.id.authenticator_dashboard_list)!!.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
        }
    private val exploreButton =
        view.findViewById<Button>(R.id.authenticator_dashboard_explore_button).apply {
            setOnClickListener { navigator.goToAuthenticatorSuggestions(true) }
        }
    private val setupButton =
        view.findViewById<Button>(R.id.authenticator_dashboard_setup_button).apply {
            setOnClickListener { viewModel.onSetupAuthenticator(setupAuthenticatorResultLauncher) }
        }
    private val seeAllOrLessButton = view.findViewById<Button>(R.id.authenticator_dashboard_see_all)
    private val editButton =
        view.findViewById<ImageButton>(R.id.authenticator_dashboard_edit_button)
            .apply { setOnClickListener { viewModel.onEditClicked() } }
    private val listener = object : Listener {
        override fun onOtpCounterUpdate(itemId: String, otp: Otp) =
            viewModel.onOtpCounterUpdate(itemId, otp)

        
        
        
        override fun onOtpCopy(code: String, itemId: String, domain: String?) {
            viewModel.onOtpCodeCopy(itemId, domain)
            copyCallback.invoke(code)
        }

        override fun onOtpDelete(item: CredentialItem, issuer: String?) {
            DialogHelper()
                .showAuthenticatorRemoveConfirmDialog(
                    fragment.requireActivity(),
                    item.title,
                    item.domain,
                    professional = item.professional,
                    issuer = issuer,
                    authenticatorLogger = authenticatorLogger
                ) {
                    
                    viewModel.onOtpRemoved(item.id)
                }
        }
    }
    private val setupAuthenticatorResultContract = SetUpAuthenticatorResultContract()
    private val setupAuthenticatorResultLauncher = setupAuthenticatorResultContract.register(
        fragment.lifecycleScope,
        fragment,
        navigator,
        viewModel
    )

    init {
        listenState(fragment, viewModel, navigator)
    }

    private fun listenState(
        fragment: Fragment,
        viewModel: AuthenticatorDashboardViewModelContract,
        navigator: Navigator
    ) {
        val lifecycle = fragment.lifecycle
        listenUiState(lifecycle, viewModel, navigator)
        listenEditState(fragment, lifecycle, viewModel)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenUiState(
        lifecycle: Lifecycle,
        viewModel: AuthenticatorDashboardViewModelContract,
        navigator: Navigator
    ) {
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is Progress -> {
                            recyclerView.isVisible = false
                            exploreButton.isVisible = false
                            setupButton.isVisible = false
                        }

                        is NoOtp -> {
                            editBackCallback.handleOnBackPressed()
                            navigator.goToAuthenticatorSuggestions(false)
                        }

                        is HasLogins -> {
                            val currentAdapter =
                                recyclerView.adapter as? AuthenticatorDashboardCredentialItemAdapter
                            val editMode = state.logins.any { it.editMode }
                            exploreButton.isVisible = !editMode
                            setupButton.isVisible = !editMode
                            recyclerView.isVisible = true
                            val currentItems = currentAdapter?.objects ?: emptyList()
                            if (currentItems.size != state.logins.size ||
                                !currentItems.containsAll(state.logins)
                            ) {
                                recyclerView.adapter =
                                    AuthenticatorDashboardCredentialItemAdapter(listener).apply {
                                        nbItemsShown = state.nbItemsShown
                                        addAll(state.logins)
                                        setupSeeButton(!allItemsShown, size())
                                    }
                            } else {
                                currentAdapter?.apply {
                                    nbItemsShown = state.nbItemsShown
                                    setupSeeButton(!allItemsShown, size())
                                    notifyDataSetChanged()
                                }
                            }
                            if (viewModel.isFirstVisit) {
                                navigator.goToGetStartedFromAuthenticator()
                                viewModel.onOnboardingDisplayed()
                            }
                        }

                        is HandleUri -> viewModel.onSetupAuthenticatorFromUri(
                            state.otpUri,
                            setupAuthenticatorResultContract
                        )
                    }
                }
            }
        }
    }

    private fun listenEditState(
        fragment: Fragment,
        lifecycle: Lifecycle,
        viewModel: AuthenticatorDashboardViewModelContract
    ) {
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.editState.collect { state ->
                    val adapter =
                        recyclerView.adapter as? AuthenticatorDashboardCredentialItemAdapter
                            ?: return@collect
                    when (state) {
                        is AuthenticatorDashboardEditState.EditLogins -> {
                            setupEditActionBar(fragment)
                            exploreButton.isVisible = false
                            setupButton.isVisible = false
                            editButton.isVisible = false
                            title.isVisible = false
                            adapter.setEditMode()
                            seeAllOrLessButton.isVisible = false
                            listContainer.background = null
                            editBackCallback.isEnabled = true
                        }

                        is AuthenticatorDashboardEditState.ViewLogins -> {
                            exploreButton.isVisible = true
                            setupButton.isVisible = true
                            editButton.isVisible = true
                            title.isVisible = true
                            adapter.setViewMode()
                            setupSeeButton(!adapter.allItemsShown, adapter.size())
                            listContainer.background = ResourcesCompat.getDrawable(
                                fragment.resources,
                                R.drawable.authenticator_list_background,
                                fragment.context?.theme
                            )
                            editBackCallback.isEnabled = false
                            
                            
                        }
                    }
                }
            }
        }
    }

    private fun setupSeeButton(someItemsHidden: Boolean, totalItems: Int) =
        seeAllOrLessButton.apply {
            isVisible =
                someItemsHidden || totalItems > DEFAULT_ITEMS_SHOWN
            val text = if (someItemsHidden) {
                R.string.authenticator_see_all
            } else {
                R.string.authenticator_see_less
            }
            setText(text)
            setOnClickListener {
                if (someItemsHidden) viewModel.onSeeAll() else viewModel.onSeeLess()
            }
        }

    private suspend fun setupEditActionBar(fragment: Fragment) {
        val activity = fragment.requireActivity()
        val actionBar = (activity as AppCompatActivity).supportActionBar!!
        if (activity is MenuContainer) activity.disableMenuAccess(true)
        actionBar.apply {
            title = fragment.getString(R.string.action_bar_title_authenticator_edit)
            
            delay(2)
            setHomeAsUpIndicator(R.drawable.ic_up_indicator_close)
        }
    }
}