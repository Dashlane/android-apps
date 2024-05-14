package com.dashlane.authenticator.suggestions

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.Companion.DEFAULT_ITEMS_SHOWN
import com.dashlane.authenticator.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.AllSetup
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.NoLogins
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.Progress
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.SetupComplete
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract
import com.dashlane.help.HelpCenterLink
import com.dashlane.navigation.Navigator
import com.dashlane.ui.widgets.view.ExpandableCardView
import com.dashlane.util.launchUrl
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.launch

class AuthenticatorSuggestionsViewProxy(
    fragment: Fragment,
    private val navigator: Navigator,
    view: View,
    viewModel: AuthenticatorSuggestionsViewModelContract,
    lifecycle: Lifecycle,
    hasSetupOtpCredentials: Boolean
) {
    private val emptyScreen =
        view.findViewById<View>(R.id.authenticator_empty_screen).apply {
            findViewById<Button>(R.id.authenticator_empty_screen_add_button)
                .setOnClickListener { navigator.goToCredentialAddStep1() }
            findViewById<Button>(R.id.authenticator_empty_screen_setup_button)
                .setOnClickListener {
                    viewModel.onSetupAuthenticator(
                        setupAuthenticatorResultLauncher
                    )
                }
        }
    private val emptyScreenAllSetup =
        view.findViewById<View>(R.id.authenticator_empty_screen_all_setup).apply {
            findViewById<Button>(R.id.authenticator_empty_screen_all_setup_add_button)
                .setOnClickListener { navigator.goToCredentialAddStep1() }
        }
    private val suggestionsScreen = view.findViewById<View>(R.id.authenticator_suggestions).apply {
        findViewById<Button>(R.id.authenticator_suggestions_setup_button).apply {
            setOnClickListener { viewModel.onSetupAuthenticator(setupAuthenticatorResultLauncher) }
            isVisible = !hasSetupOtpCredentials
        }
    }
    private val faq = view.findViewById<View>(R.id.authenticator_faq)
    private val faqTitle = view.findViewById<View>(R.id.authenticator_faq_title)
    private val suggestionsRecycleView =
        suggestionsScreen.findViewById<RecyclerView>(R.id.authenticator_suggestions_list).apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
        }
    private val seeAllOrLessButton =
        suggestionsScreen.findViewById<Button>(R.id.authenticator_suggestions_see_all)
    private val otpResultLauncher =
        fragment.registerForActivityResult(AuthenticatorIntroResult()) { (id, otp) ->
            if (id != null && otp != null) viewModel.onOtpSetup(id, otp)
        }
    private val listener =
        EfficientAdapter.OnItemClickListener<HasLogins.CredentialItem> { _, _, item, _ ->
            otpResultLauncher.launch(item)
        }
    private val setupAuthenticatorResultLauncher =
        SetUpAuthenticatorResultContract().register(
            fragment.lifecycleScope,
            fragment,
            navigator,
            viewModel
        )

    private val expandableCards = listOf<ExpandableCardView>(
        
        view.findViewById(R.id.authenticator_question_1)!!,
        
        view.findViewById(R.id.authenticator_question_2)!!,
        
        view.findViewById(R.id.authenticator_question_3)!!
    )

    init {
        setupFaqCards()
        listenState(lifecycle, viewModel)
    }

    private fun listenState(
        lifecycle: Lifecycle,
        viewModel: AuthenticatorSuggestionsViewModelContract
    ) = lifecycle.coroutineScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.uiState.collect { state ->
                when (state) {
                    is Progress -> {
                        emptyScreenAllSetup.isVisible = false
                        emptyScreen.isVisible = false
                        suggestionsScreen.isVisible = false
                        faqVisible(true)
                    }
                    is NoLogins -> {
                        emptyScreenAllSetup.isVisible = false
                        emptyScreen.isVisible = true
                        suggestionsScreen.isVisible = false
                        faqVisible(true)
                        mayShowOnboarding(viewModel)
                    }
                    is HasLogins -> {
                        emptyScreen.isVisible = false
                        emptyScreenAllSetup.isVisible = false
                        suggestionsScreen.isVisible = true
                        suggestionsRecycleView.adapter =
                            AuthenticatorSuggestionsCredentialItemAdapter().apply {
                                addAll(state.logins.take(state.nbItemsShown))
                                onItemClickListener = listener
                            }
                        val someItemsHidden = state.logins.size > state.nbItemsShown
                        seeAllOrLessButton.apply {
                            isVisible = someItemsHidden || state.logins.size > DEFAULT_ITEMS_SHOWN
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
                        
                        
                        faqVisible(someItemsHidden || state.logins.size <= DEFAULT_ITEMS_SHOWN)
                        mayShowOnboarding(viewModel)
                    }
                    is AllSetup -> {
                        emptyScreenAllSetup.isVisible = true
                        emptyScreen.isVisible = false
                        suggestionsScreen.isVisible = false
                        faqVisible(true)
                        mayShowOnboarding(viewModel)
                    }
                    is SetupComplete -> navigator.popBackStack()
                }
            }
        }
    }

    private fun faqVisible(visible: Boolean) {
        faq.isVisible = visible
        faqTitle.isVisible = visible
    }

    private fun mayShowOnboarding(viewModel: AuthenticatorSuggestionsViewModelContract) {
        if (viewModel.isFirstVisit) {
            navigator.goToGetStartedFromAuthenticatorSuggestions()
            viewModel.onOnboardingDisplayed()
        }
    }

    private fun setupFaqCards() {
        ViewCompat.setAccessibilityHeading(faqTitle, true)
        expandableCards.forEachIndexed { index, expandableCard ->
            expandableCard.setOnExpandListener { expanded ->
                if (!expanded) return@setOnExpandListener
                val body = expandableCard.findViewById<TextView>(R.id.authenticator_faq_question_body)
                body.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                
                expandableCards.filter { it.id != expandableCard.id }.forEach {
                    it.setExpanded(expanded = false, withAnimation = true)
                }
            }
            val button = expandableCard.findViewById<Button>(R.id.authenticator_faq_question_button)
            when (index) {
                0, 1 -> button.setOnClickListener {
                    button.context.launchUrl(HelpCenterLink.ARTICLE_AUTHENTICATOR.uri)
                }
                else -> button.setOnClickListener {
                    button.context.launchUrl("https://support.dashlane.com/hc/requests/new")
                }
            }
        }
    }
}