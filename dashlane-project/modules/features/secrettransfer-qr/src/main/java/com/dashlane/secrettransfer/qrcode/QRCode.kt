package com.dashlane.secrettransfer.qrcode

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.secrettransferqr.R

@Composable
fun QRCode(
    modifier: Modifier = Modifier,
    qrCode: Bitmap,
) {
    Image(
        bitmap = qrCode.asImageBitmap(),
        contentDescription = stringResource(id = R.string.and_accessibility_secret_transfer_qrcode),
        modifier = modifier
    )
}

@Preview
@Composable
private fun QRCodePreview() {
    DashlanePreview {
        QRCode(
            qrCode = QrCodeGenerator.generateBitmapFromUri(
                secretTransferUri = "",
                size = 50,
                color = DashlaneTheme.colors.textNeutralCatchy.value.toArgb()
            )
        )
    }
}
