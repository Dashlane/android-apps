package com.dashlane.login.pages.secrettransfer.choosetype

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.ButtonLarge
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.pages.secrettransfer.qrcode.DashlaneLogo

@Composable
fun ChooseTypeScreen(
    modifier: Modifier = Modifier,
    viewModel: ChooseTypeViewModel,
    email: String? = null,
    onGoToUniversal: (String?) -> Unit,
    onGoToQr: (String?) -> Unit,
    onGoToHelp: (String?) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.viewStarted(email)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is ChooseTypeState.GoToHelp -> {
                viewModel.viewNavigated()
                onGoToHelp(uiState.data.email)
            }
            is ChooseTypeState.GoToQR -> {
                viewModel.viewNavigated()
                onGoToQr(uiState.data.email)
            }
            is ChooseTypeState.GoToUniversal -> {
                viewModel.viewNavigated()
                onGoToUniversal(uiState.data.email)
            }
            else -> Unit
        }
    }

    ChooseTypeContent(
        modifier = modifier,
        onComputerClicked = viewModel::computerClicked,
        onMobileClicked = viewModel::mobileClicked,
        onHelpClicked = viewModel::helpClicked
    )
}

@Composable
fun ChooseTypeContent(
    modifier: Modifier = Modifier,
    onComputerClicked: () -> Unit,
    onMobileClicked: () -> Unit,
    onHelpClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        DashlaneLogo()
        Text(
            text = stringResource(id = R.string.login_secret_transfer_choose_type_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 72.dp)
        )
        ButtonLarge(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = onComputerClicked,
            layout = ButtonLayout.IconLeading(
                IconTokens.laptopOutlined,
                stringResource(R.string.login_secret_transfer_choose_type_computer_button)
            ),
            mood = Mood.Brand,
            intensity = Intensity.Quiet
        )
        ButtonLarge(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = onMobileClicked,
            layout = ButtonLayout.IconLeading(
                IconTokens.itemPhoneMobileOutlined,
                stringResource(R.string.login_secret_transfer_choose_type_mobile_button)
            ),
            mood = Mood.Brand,
            intensity = Intensity.Quiet
        )
        Spacer(modifier = Modifier.weight(1.0f))
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 32.dp),
            onClick = onHelpClicked,
            intensity = Intensity.Quiet,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.login_secret_transfer_choose_type_recovery_button)
            )
        )
    }
}

@Preview
@Composable
fun ChooseTypeContentPreview() {
    DashlanePreview {
        ChooseTypeContent(
            onComputerClicked = { },
            onMobileClicked = { },
            onHelpClicked = { }
        )
    }
}
