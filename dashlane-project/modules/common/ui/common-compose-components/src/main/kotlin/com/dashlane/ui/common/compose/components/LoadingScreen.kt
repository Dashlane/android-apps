package com.dashlane.ui.common.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    title: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = DashlaneTheme.colors.textBrandQuiet.value,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = title,
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
fun LoadingScreenPreview() = DashlanePreview {
    LoadingScreen(title = "Loading")
}
