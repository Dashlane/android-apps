package com.dashlane.secrettransfer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.navArgs
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.Navigator
import com.dashlane.secrettransfer.view.intro.SecretTransferIntroScreen
import com.dashlane.secrettransfer.view.universal.pending.SecretTransferPendingScreen
import com.dashlane.ui.widgets.compose.LoadingScreen
import com.dashlane.util.compose.BackPressedDispatcherBridge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SecretTransferFragment : Fragment() {

    @Inject
    lateinit var navigator: Navigator

    private val args: SecretTransferFragmentArgs by navArgs()
    private val viewModel by viewModels<SecretTransferViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        activity?.let {
            val menuProvider = BackPressedDispatcherBridge.getMenuProvider(it)
            it.addMenuProvider(menuProvider, viewLifecycleOwner)
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    SecretTransferScreen(
                        viewModel = viewModel,
                        onCancel = { navigator.popBackStack() },
                        onSuccess = { navigator.popBackStack() },
                        deepLinkTransferId = args.id,
                        deepLinkKey = args.key
                    )
                }
            }
        }
    }
}

@Composable
fun SecretTransferScreen(
    viewModel: SecretTransferViewModel,
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
    deepLinkTransferId: String?,
    deepLinkKey: String?
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    Crossfade(targetState = uiState, label = "secretTransferCrossfade") { state ->
        when (state) {
            is SecretTransferState.GoToIntro -> SecretTransferIntroScreen(
                viewModel = hiltViewModel(),
                isPasswordless = state.isPasswordless,
                onCancel = onCancel,
                onSuccess = onSuccess,
                onRefresh = viewModel::onRefreshClicked,
                deepLinkTransferId = deepLinkTransferId,
                deepLinkKey = deepLinkKey
            )
            SecretTransferState.Loading,
            SecretTransferState.Initial -> LoadingScreen(title = "")
            is SecretTransferState.ShowTransfer -> SecretTransferPendingScreen(
                viewModel = hiltViewModel(),
                secretTransfer = state.transfer,
                onCancel = onCancel,
                onSuccess = onSuccess,
            )
        }
    }
}
