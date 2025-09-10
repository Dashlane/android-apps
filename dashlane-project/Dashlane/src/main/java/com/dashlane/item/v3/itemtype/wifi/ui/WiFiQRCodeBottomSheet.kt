package com.dashlane.item.v3.itemtype.wifi.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.dashlane.R
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.secrettransfer.qrcode.QRCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun WiFiQRCodeBottomSheet(
    modifier: Modifier = Modifier,
    title: String,
    qrCodeContent: String,
    onDismiss: () -> Unit,
) {
    BottomSheetContent(
        modifier = modifier.navigationBarsPadding(),
        title = title,
        description = stringResource(R.string.wifi_qr_code_bottom_sheet_description),
        extraContent = {
            QRCode(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DashlaneTheme.colors.containerAgnosticNeutralQuiet)
                    .align(Alignment.CenterHorizontally),
                qrCode = rememberQrCode(qrCodeContent),
            )
        },
        actions = BottomSheetActions(
            primaryButtonText = stringResource(id = R.string.wifi_qr_code_bottom_sheet_done),
            onPrimaryButtonClick = onDismiss,
            secondaryButtonText = null,
            onSecondaryButtonClick = null,
        ),
    )
}

@Composable
private fun rememberQrCode(content: String): Bitmap {
    val color = DashlaneTheme.colors.textNeutralCatchy.value.toArgb()
    val density = LocalDensity.current
    val size = remember {
        with(density) { 268.dp.toPx().toInt() }
    }
    return remember(content) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix[x, y]) color else Color.Transparent.toArgb()
            }
        }
        bitmap
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WifiQRCodeBottomSheetLightPreview() =
    DashlaneModalBottomSheetPreview {
        WiFiQRCodeBottomSheet(
            title = "Paris Office",
            qrCodeContent = "Wifi QR code content",
            onDismiss = { },
        )
    }