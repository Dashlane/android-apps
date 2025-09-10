package com.dashlane.secrettransfer.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.secrettransfer.ui.R
import com.dashlane.secrettransfer.view.intro.SecretTransferIntroScreen
import com.dashlane.secrettransfer.view.universal.pending.SecretTransferPendingScreen
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.LoadingScreen
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState
import com.dashlane.util.compose.BackPressedDispatcherBridge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecretTransferActivity : DashlaneActivity() {
    private val viewModel by viewModels<SecretTransferViewModel>()

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val args = intent.extras?.let { SecretTransferActivityArgs.fromBundle(it) }
        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)

        setContent {
            DashlaneTheme {
                val topBarState by rememberSaveable {
                    mutableStateOf(TopBarState(title = getString(R.string.secret_transfer_settings_entry_title)))
                }

                TopBarScaffold(
                    topBarState = topBarState,
                    back = ::finish,
                ) { contentPadding ->
                    SecretTransferScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = viewModel,
                        onCancel = ::finish,
                        onSuccess = ::finish,
                        deepLinkTransferId = args?.id,
                        deepLinkKey = args?.key,
                    )
                }
            }
        }
    }
}

@Composable
fun SecretTransferScreen(
    modifier: Modifier = Modifier,
    viewModel: SecretTransferViewModel,
    onCancel: () -> Unit,
    onSuccess: () -> Unit,
    deepLinkTransferId: String?,
    deepLinkKey: String?,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    Crossfade(targetState = uiState, label = "secretTransferCrossfade") { state ->
        when (state) {
            is SecretTransferState.GoToIntro -> SecretTransferIntroScreen(
                modifier = modifier,
                viewModel = hiltViewModel(),
                isPasswordless = state.isPasswordless,
                onCancel = onCancel,
                onSuccess = onSuccess,
                onRefresh = viewModel::onRefreshClicked,
                deepLinkTransferId = deepLinkTransferId,
                deepLinkKey = deepLinkKey,
            )

            SecretTransferState.Loading,
            SecretTransferState.Initial,
            -> LoadingScreen(title = "")

            is SecretTransferState.ShowTransfer -> SecretTransferPendingScreen(
                modifier = modifier,
                viewModel = hiltViewModel(),
                secretTransfer = state.transfer,
                onCancel = onCancel,
                onSuccess = onSuccess,
            )
        }
    }
}