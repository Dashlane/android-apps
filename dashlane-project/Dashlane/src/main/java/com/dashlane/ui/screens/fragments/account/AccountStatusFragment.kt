package com.dashlane.ui.screens.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.dashlane.design.component.Card
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.SnackbarType
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DisplayFieldActions
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountStatusFragment : AbstractContentFragment() {

    private val viewModel: AccountStatusViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    val uiState by viewModel.uiState.viewState.collectAsStateWithLifecycle()
                    LaunchedEffect(key1 = viewModel) {
                        viewModel.uiState.sideEffect.collect { navState ->
                            when (navState) {
                                is AccountStatusNavState.EditContactForm -> navigator.goToChangeContactEmail()
                            }
                        }
                    }
                    AccountStatusScreen(uiState)
                }
            }
            viewModel.loadEmails()
        }
    }

    @Composable
    fun AccountStatusScreen(viewState: AccountStatusViewState) {
        val snackbarState = remember { SnackbarHostState() }
        LaunchedEffect(viewState) {
            if (viewState.isError) {
                snackbarState.showSnackbar(requireContext().getString(R.string.network_failed_notification))
            }
        }
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                LoginDetailsFrame(viewState.loginEmail)
                Spacer(modifier = Modifier.height(16.dp))
                AccountVerificationFrame(viewState.contactEmail)
            }
            DashlaneSnackbarWrapper(type = SnackbarType.ERROR) {
                SnackbarHost(modifier = Modifier.align(Alignment.BottomCenter), hostState = snackbarState)
            }
        }
    }

    @Composable
    fun LoginDetailsFrame(loginEmail: String?) {
        Card {
            Column {
                Text(
                    text = stringResource(id = R.string.account_status_login_details),
                    style = DashlaneTheme.typography.titleBlockMedium,
                    color = DashlaneTheme.colors.textNeutralCatchy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.account_status_login_details_email),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.account_status_login_details_help),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet
                )
                Spacer(modifier = Modifier.height(16.dp))
                DisplayField(
                    value = loginEmail ?: "",
                    label = stringResource(id = R.string.account_status_login_details_email_field)
                )
            }
        }
    }

    @Composable
    fun AccountVerificationFrame(contactEmail: String?) {
        Card {
            Column {
                Text(
                    text = stringResource(id = R.string.account_status_account_verification),
                    style = DashlaneTheme.typography.titleBlockMedium,
                    color = DashlaneTheme.colors.textNeutralCatchy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(id = R.string.account_status_account_verification_details),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet
                )
                Spacer(modifier = Modifier.height(16.dp))

                DisplayField(
                    label = stringResource(id = R.string.account_status_account_verification_contact_field),
                    value = contactEmail ?: "",
                    actions = DisplayFieldActions.newInstance(
                        FieldAction.Generic(
                            iconLayout = ButtonLayout.IconOnly(
                                iconToken = IconTokens.actionEditOutlined,
                                contentDescription = stringResource(id = R.string.account_status_account_verification_edit_description)
                            )
                        ) {
                            viewModel.onClickEditContactEmail(contactEmail)
                            true
                        }
                    )
                )
            }
        }
    }

    @Preview
    @Composable
    private fun AccountVerificationPreview() {
        DashlanePreview {
            AccountVerificationFrame(
                contactEmail = "randomemail@provider.com"
            )
        }
    }

    @Preview
    @Composable
    private fun LoginDetailsPreview() {
        DashlanePreview {
            LoginDetailsFrame(
                loginEmail = "randomemail@provider.com"
            )
        }
    }

    @Preview
    @Composable
    private fun AccountStatusDisplayFormPreview() {
        DashlanePreview {
            AccountStatusScreen(
                AccountStatusViewState(
                    loginEmail = "randomemail@provider.com",
                    contactEmail = "randomemail@provider.com"
                )
            )
        }
    }
}