package com.dashlane.autofillapi

sealed class ChromeAutofillStatus(open val packageName: String) {
    data class ThirdPartyAutofillActivated(override val packageName: String) : ChromeAutofillStatus(packageName)

    data class DefaultAutofill(override val packageName: String) : ChromeAutofillStatus(packageName)

    data class CompatibilityAutofill(override val packageName: String = "") : ChromeAutofillStatus(packageName)

    enum class ChromePackage(val packageName: String) {
        
        STABLE("com.android.chrome"),
        BETA("com.chrome.beta"),
        CANARY("com.chrome.canary"),
        DEV("com.chrome.dev"),
    }
}