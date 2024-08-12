package com.dashlane.vault.textfactory.list

data class StatusText(
    val text: String,
    val isWarning: Boolean = false,
    val textToHighlight: String? = null,
)

fun String.toStatusText(): StatusText = StatusText(text = this)