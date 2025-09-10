package com.dashlane.ui.activities.firstpassword.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.component.ButtonLargeBar
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.PasswordField
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.tooling.FieldAction
import com.dashlane.design.component.tooling.TextFieldActions
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.activities.firstpassword.AddFirstPasswordState
import com.dashlane.ui.activities.firstpassword.AddFirstPasswordViewModel
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import com.dashlane.ui.common.compose.components.components.DashlaneTopAppBar
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon
import com.dashlane.xml.domain.SyncObfuscatedValue

@Composable
fun AddFirstPasswordScreen(
    viewModel: AddFirstPasswordViewModel,
    navigateUp: () -> Unit,
    goToAutofillDemo: (String, String, SyncObfuscatedValue) -> Unit,
    goToHomeOrChecklist: () -> Unit,
    finish: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                AddFirstPasswordState.SideEffect.FinishOnError -> {
                    finish()
                }

                is AddFirstPasswordState.SideEffect.GoToAutofillDemo -> {
                    goToAutofillDemo(sideEffect.url, sideEffect.login, sideEffect.password)
                }

                AddFirstPasswordState.SideEffect.GoToHomeOrChecklist -> {
                    goToHomeOrChecklist()
                }
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    AddFirstPasswordContent(
        urlForThumbnail = uiState.urlForThumbnail,
        email = uiState.email,
        password = uiState.password,
        isPasswordObfuscated = uiState.isPasswordObfuscated,
        isSaveButtonEnabled = uiState.isSaveButtonEnabled,
        showFaq = uiState.showFAQ,
        showAutofillDemoPrompt = uiState.showAutofillDemoPrompt,
        onNavigateUp = navigateUp,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onHideOrRevealPasswordClick = viewModel::onHideOrRevealPasswordClick,
        onShowFaqClick = viewModel::onShowFAQClicked,
        onFaqDismiss = viewModel::dismissFAQ,
        onSaveClick = viewModel::onSaveCredentialClicked,
        onAutofillDemoPromptDismiss = viewModel::onDismissAutofillDemoClicked,
        onTryDemoClick = viewModel::onTryAutofillDemoClicked,
    )
}

@Suppress("kotlin:S107")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFirstPasswordContent(
    urlForThumbnail: String,
    email: String,
    password: String,
    isPasswordObfuscated: Boolean,
    isSaveButtonEnabled: Boolean,
    showFaq: Boolean,
    showAutofillDemoPrompt: Boolean,
    onNavigateUp: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onHideOrRevealPasswordClick: () -> Unit,
    onShowFaqClick: () -> Unit,
    onSaveClick: () -> Unit,
    onFaqDismiss: () -> Unit,
    onAutofillDemoPromptDismiss: () -> Unit,
    onTryDemoClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.scaffoldBackgroundAndInsets(),
        containerColor = DashlaneTheme.colors.backgroundAlternate,
        topBar = {
            AddFirstPasswordTopBar(
                urlForThumbnail = urlForThumbnail,
                onNavigateUp = onNavigateUp,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            AddCredentialContent(
                email = email,
                password = password,
                isPasswordObfuscated = isPasswordObfuscated,
                isSaveButtonEnabled = isSaveButtonEnabled,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onHideOrRevealPasswordClick = onHideOrRevealPasswordClick,
                onShowFaqClick = onShowFaqClick,
                onSaveClick = onSaveClick,
            )

            if (showFaq) {
                ModalBottomSheet(
                    onDismissRequest = onFaqDismiss,
                ) {
                    AddFirstPasswordFAQContent()
                }
            }

            if (showAutofillDemoPrompt) {
                ModalBottomSheet(
                    onDismissRequest = onAutofillDemoPromptDismiss,
                ) {
                    AutofillDemoPromptBottomSheetContent(
                        onTryDemoClick = { onTryDemoClick() },
                        onReturnHomeClick = onAutofillDemoPromptDismiss,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFirstPasswordTopBar(
    urlForThumbnail: String,
    onNavigateUp: () -> Unit,
) {
    DashlaneTopAppBar(
        title = {
            ThumbnailDomainIcon(
                modifier = Modifier.semantics { contentDescription = urlForThumbnail },
                urlDomain = urlForThumbnail,
                size = ThumbnailSize.Small,
            )
        },
        navigationAction = DashlaneTopAppBar.NavigationAction(
            iconToken = IconTokens.arrowLeftOutlined,
            contentDescription = stringResource(R.string.and_accessibility_back_button),
            onClick = onNavigateUp,
        ),
    )
}

@SuppressWarnings("kotlin:S107", "LongMethod")
@Composable
private fun AddCredentialContent(
    email: String,
    password: String,
    isPasswordObfuscated: Boolean,
    isSaveButtonEnabled: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onHideOrRevealPasswordClick: () -> Unit,
    onShowFaqClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Column(
        modifier = Modifier
            .widthIn(480.dp)
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, true),
        ) {
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            Text(
                modifier = Modifier.semantics {
                    heading()
                },
                text = stringResource(R.string.add_first_password_title),
                style = DashlaneTheme.typography.titleSectionMedium,
                color = DashlaneTheme.colors.textNeutralCatchy,
            )
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.add_first_password_email_hint),
                value = email,
                onValueChange = onEmailChange,
            )
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            PasswordField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = stringResource(R.string.add_first_password_password_hint),
                value = password,
                onValueChange = onPasswordChange,
                obfuscated = isPasswordObfuscated,
                actions = TextFieldActions.Password(
                    hideRevealAction = FieldAction.HideReveal(
                        resources = LocalContext.current.resources,
                        onClick = {
                            onHideOrRevealPasswordClick()
                            true
                        },
                    ),
                    genericAction = null,
                    passwordGeneratorAction = null,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = if (isSaveButtonEnabled) {
                        { onSaveClick() }
                    } else {
                        null
                    },
                ),
            )
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            LinkButton(
                text = stringResource(R.string.add_first_password_how_is_this_secure),
                destinationType = LinkButtonDestinationType.INTERNAL,
                onClick = onShowFaqClick,
            )
        }
        ButtonLargeBar(
            modifier = Modifier.padding(vertical = 16.dp),
            primaryText = stringResource(R.string.add_first_password_save),
            isPrimaryButtonEnabled = isSaveButtonEnabled,
            onPrimaryButtonClick = onSaveClick,
            secondaryText = null,
            onSecondaryButtonClick = null,
        )
    }
}

@Preview
@Composable
private fun AddFirstPasswordTopBarPreview() {
    DashlanePreview {
        AddFirstPasswordTopBar(
            urlForThumbnail = "twitch.tv",
            onNavigateUp = {},
        )
    }
}

@Preview
@Composable
private fun AddCredentialContentPreview() {
    DashlanePreview {
        var email by remember { mutableStateOf("randomemail@provider.com") }
        var password by remember { mutableStateOf("") }
        var isPasswordObfuscated by remember { mutableStateOf(true) }
        AddCredentialContent(
            email = email,
            password = password,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            isPasswordObfuscated = isPasswordObfuscated,
            onHideOrRevealPasswordClick = { isPasswordObfuscated = !isPasswordObfuscated },
            isSaveButtonEnabled = email.isNotEmpty() && password.isNotEmpty(),
            onShowFaqClick = {},
            onSaveClick = {},
        )
    }
}