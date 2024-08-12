package com.dashlane.premium.paywall.trialended

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink
import com.dashlane.navigation.Navigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrialEndedFragment : DialogFragment() {

    @Inject
    lateinit var navigator: Navigator

    private val viewModel by viewModels<TrialEndedViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    val state = viewModel.trialState.collectAsStateWithLifecycle()
                    LaunchedEffect(key1 = state) {
                        when (state.value) {
                            FreeTrialScreenState.Init -> viewModel.reload()
                            is FreeTrialScreenState.Loaded -> Unit
                        }
                    }

                    TrialEndedScreen(
                        trialEndedState = state.value,
                        onClickLink = { _ ->
                            HelpCenterCoordinator.openLink(
                                helpCenterLink = HelpCenterLink.ARTICLE_ABOUT_FREE_PLAN_CHANGES,
                                context = requireActivity()
                            )
                        },
                        onCloseClick = {
                            closeDialog()
                        },
                        onSeePlansClick = {
                            navigator.goToOffers(offerType = null)
                            closeDialog()
                        }
                    )
                }
            }
        }
    }

    private fun closeDialog() {
        dismiss()
    }
}