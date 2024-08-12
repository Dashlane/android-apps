package com.dashlane.ui.illustration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.design.theme.typography.robotoMono
import com.dashlane.ui.illustrationsresources.R

@Composable
fun WebUrlIllustration(url: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        Image(
            modifier = Modifier.align(Alignment.TopCenter),
            painter = painterResource(id = R.drawable.img_m2w_url),
            contentDescription = ""
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 38.dp)
                .wrapContentHeight()
                .width(257.dp)
                .background(
                    shape = RoundedCornerShape(size = 5.dp),
                    color = DashlaneTheme.colors.containerExpressiveBrandCatchyIdle
                ),
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                maxLines = 1,
                text = url,
                color = DashlaneTheme.colors.textInverseCatchy,
                style = TextStyle(
                    fontSize = with(LocalDensity.current) { 18.dp.toSp() },
                    fontFamily = robotoMono,
                    fontWeight = FontWeight.Bold,
                ),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun WebUrlIllustrationPreview() = DashlanePreview {
    val modifier = Modifier.fillMaxWidth()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(
            16.dp
        )
    ) {
        WebUrlIllustration(
            "dashlane.com/web",
            modifier
        )
        WebUrlIllustration(
            "ThisDomainIsAbsurdlyLongAndShouldNeverBeenUsed.ButAtLeastThisComposableDoesNotBreakBecauseOfIt.com",
            modifier
        )
    }
}