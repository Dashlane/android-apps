package com.dashlane.wear.login.remote.qr.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.secrettransfer.qrcode.QRCode
import com.dashlane.secrettransfer.qrcode.QrCodeGenerator
import com.dashlane.wear.ui.DashlaneWearPreview
import com.google.android.horologist.compose.layout.fillMaxRectangle

@Composable
internal fun WearQrCode(
    qrCode: Bitmap? = null,
) {
    qrCode?.let { bitmap ->
        QRCode(
            modifier = Modifier
                .fillMaxRectangle()
                .background(
                    shape = RoundedCornerShape(8.dp),
                    color = DashlaneTheme.colors.textNeutralCatchy.value,
                ),
            qrCode = bitmap,
            contentScale = ContentScale.Fit,
        )
    }
}

internal fun getQRCodeSizeForConfiguration(density: Density, configuration: Configuration) =
    with(density) { configuration.screenWidthDp.dp.roundToPx() }

@WearPreviewSmallRound
@Composable
private fun WearQrCodePreview() {
    DashlaneWearPreview {
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        WearQrCode(
            qrCode = QrCodeGenerator.generateBitmapFromUri(
                secretTransferUri = "https://dashlane.com/asdasdasdfdfdsdsfadfa",
                size = getQRCodeSizeForConfiguration(density, configuration),
                color = Color.Black.toArgb(),
            ),
        )
    }
}