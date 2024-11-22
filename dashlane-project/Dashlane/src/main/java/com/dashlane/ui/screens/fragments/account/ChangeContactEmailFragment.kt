package com.dashlane.ui.screens.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.SnackbarType
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.activities.fragments.AbstractContentFragment

class ChangeContactEmailFragment : AbstractContentFragment() {
    private val viewModel: ChangeContactEmailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    val snackbarHostState = remember { SnackbarHostState() }
                    val uiState by viewModel.uiState.viewState.collectAsStateWithLifecycle()
                    LaunchedEffect(key1 = viewModel) {
                        viewModel.uiState.sideEffect.collect { navState ->
                            when (navState) {
                                ChangeContactEmailNavState.Finish -> navigator.popBackStack()
                            }
                        }
                    }

                    if (!uiState.isLoading) {
                        LaunchedEffect(uiState) {
                            if (uiState.isError) {
                                snackbarHostState.showSnackbar(requireContext().getString(R.string.network_failed_notification))
                            }
                        }
                        ChangeContactEmailScreen(
                            uiState.currentContactEmail,
                            uiState.newContactEmail,
                            uiState.isSaveEnabled,
                            viewModel::onNewContactEmailChange,
                            viewModel::onSendNewContactEmailClicked,
                            snackbarHostState
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ChangeContactEmailScreen(
        currentContactEmail: String?,
        newContactEmail: String?,
        saveEnabled: Boolean,
        onNewContactEmailChange: (String) -> Unit,
        onSendNewContactEmailClicked: (String?) -> Unit,
        snackbarHostState: SnackbarHostState
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.account_status_change_contact_email),
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {},
                    label = stringResource(id = R.string.account_status_change_contact_email_current),
                    value = currentContactEmail ?: "",
                    readOnly = true
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = newContactEmail ?: "",
                    onValueChange = onNewContactEmailChange,
                    label = stringResource(id = R.string.account_status_change_contact_email_new)
                )
                ButtonMedium(
                    modifier = Modifier.align(alignment = Alignment.End),
                    onClick = { onSendNewContactEmailClicked(newContactEmail) },
                    layout = ButtonLayout.TextOnly(stringResource(id = R.string.account_status_change_contact_email_confirm)),
                    enabled = saveEnabled
                )
            }
            DashlaneSnackbarWrapper(type = SnackbarType.ERROR) {
                SnackbarHost(modifier = Modifier.align(Alignment.BottomCenter), hostState = snackbarHostState)
            }
        }
    }

    @Preview
    @Composable
    private fun ChangeContactEmailScreenPreview() {
        DashlanePreview {
            ChangeContactEmailScreen(
                currentContactEmail = "randomemail@provider.com",
                newContactEmail = "randomemail@provider.com",
                onNewContactEmailChange = {},
                onSendNewContactEmailClicked = {},
                saveEnabled = true,
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }

    @Preview
    @Composable
    private fun ChangeContactEmailScreenActionDisabledPreview() {
        DashlanePreview {
            ChangeContactEmailScreen(
                currentContactEmail = "randomemail@provider.com",
                newContactEmail = "new@dash",
                onNewContactEmailChange = {},
                onSendNewContactEmailClicked = {},
                saveEnabled = false,
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }
}
