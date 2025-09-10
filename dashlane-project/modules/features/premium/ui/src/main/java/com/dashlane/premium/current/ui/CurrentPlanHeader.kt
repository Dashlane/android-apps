package com.dashlane.premium.current.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.premium.current.ui.CurrentPlanDefaults.constraintContent
import com.dashlane.premium.ui.R

@Composable
internal fun CurrentPlanHeader() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val modifier = if (maxWidth >= 480.dp) {
            Modifier
                .fillMaxWidth()
                .height(112.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(480f / 112f, matchHeightConstraintsFirst = true)
        }
        Box(
            modifier = modifier,
            contentAlignment = Alignment.TopCenter,
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.current_plan_header_background),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
            )
            Box(
                modifier = Modifier
                    .constraintContent()
                    .padding(horizontal = CurrentPlanDefaults.horizontalPadding),
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(46f / 103, matchHeightConstraintsFirst = true),
                    painter = painterResource(R.drawable.ic_current_plan_diamond),
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CurrentPlanHeaderPreview() = DashlaneTheme {
    CurrentPlanHeader()
}