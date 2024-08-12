import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview

@Composable
internal fun PricingBox(
    billedPrice: String,
    barredPrice: String? = null,
    additionalInfo: String? = null
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            barredPrice?.let {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = barredText(it),
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet
                )
            }
            Text(
                text = boldText(billedPrice),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard
            )
        }
        additionalInfo?.let {
            Text(
                text = additionalInfo,
                style = DashlaneTheme.typography.bodyHelperRegular,
                color = DashlaneTheme.colors.textNeutralQuiet,
            )
        }
    }
}

@Composable
private fun barredText(it: String) = buildAnnotatedString {
    withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
        append(it)
    }
}

@Composable
private fun boldText(it: String) = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append(it)
    }
}

@Preview
@Composable
private fun PricingBoxPreview() = DashlanePreview {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        
        PricingBox(
            billedPrice = "$59 /year"
        )

        
        PricingBox(
            billedPrice = "$59 /year",
            barredPrice = "$79 /year",
            additionalInfo = "Then $79 /year",
        )
    }
}