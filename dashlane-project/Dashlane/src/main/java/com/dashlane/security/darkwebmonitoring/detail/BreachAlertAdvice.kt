package com.dashlane.security.darkwebmonitoring.detail

data class BreachAlertAdvice(
    val content: String,
    val resolved: Boolean,
    val buttonText: String? = null,
    val buttonAction: (() -> Unit)? = null
)