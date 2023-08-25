package com.dashlane.core.helpers

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppSignature(
    val packageName: String,
    val sha256Signatures: List<String>? = null,
    val sha512Signatures: List<String>? = null
) : Parcelable {
    fun hasSignatures(): Boolean {
        return !sha256Signatures.isNullOrEmpty() || !sha512Signatures.isNullOrEmpty()
    }
}
