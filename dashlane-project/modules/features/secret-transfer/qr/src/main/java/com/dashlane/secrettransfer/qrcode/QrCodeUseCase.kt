package com.dashlane.secrettransfer.qrcode

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.ColorInt
import com.google.zxing.EncodeHintType

fun generateQrCodeBitmap(
    uri: Uri?,
    size: Int,
    @ColorInt color: Int,
    marginSize: Int = 4,
): Bitmap = QrCodeGenerator.generateBitmapFromUri(
    secretTransferUri = uri.toString(),
    size = size,
    
    color = color,
    
    
    encodeHints = mapOf(EncodeHintType.MARGIN to marginSize),
)