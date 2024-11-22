package com.dashlane.secrettransfer.qrcode

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.ColorInt

fun generateQrCodeBitmap(uri: Uri?, size: Int, @ColorInt color: Int): Bitmap = QrCodeGenerator.generateBitmapFromUri(
    secretTransferUri = uri.toString(),
    size = size,
    
    color = color
)