package com.dashlane.core.helpers

data class AppSignature(
    val packageName: String,
    val sha256Signatures: List<String>? = null,
    val sha512Signatures: List<String>? = null,
) {
    fun hasSignatures(): Boolean {
        return !sha256Signatures.isNullOrEmpty() || !sha512Signatures.isNullOrEmpty()
    }
}