package com.dashlane.secrettransfer.view.success

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransfer.R

@Composable
fun SecretTransferSuccess(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(78.dp)
                .height(78.dp),
            painter = painterResource(id = R.drawable.ic_checklist_check),
            contentDescription = stringResource(id = R.string.and_accessibility_secret_transfer_success),
            colorFilter = ColorFilter.tint(DashlaneTheme.colors.textBrandQuiet.value)
        )
        Text(
            text = stringResource(id = R.string.secret_transfer_screen_success_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 40.dp)
        )
    }
}

@Preview
@Composable
private fun SecretTransferSuccessPreview() {
    DashlanePreview { SecretTransferSuccess() }
}
