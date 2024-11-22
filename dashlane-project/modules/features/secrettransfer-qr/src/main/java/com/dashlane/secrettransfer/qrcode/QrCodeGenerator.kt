package com.dashlane.secrettransfer.qrcode

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    fun generateBitmapFromUri(
        secretTransferUri: String,
        size: Int,
        color: Int
    ): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(secretTransferUri, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) color else Color.Transparent.toArgb())
            }
        }
        return bitmap
    }
}