package com.dashlane.securearchive.contract

data class BackupActivityResult(
    val isSuccessful: Boolean,
    val isShared: Boolean,
    val count: Int,
)