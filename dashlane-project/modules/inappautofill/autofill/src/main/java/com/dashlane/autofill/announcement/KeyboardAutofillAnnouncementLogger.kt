package com.dashlane.autofill.announcement



interface KeyboardAutofillAnnouncementLogger {
    

    fun logDisplayWithAutofillOn()

    

    fun logDisplayWithAutofillOff()

    

    fun logPositiveWithAutofillOn()

    

    fun logPositiveWithAutofillOff()

    

    fun logNegativeWithAutofillOff()

    enum class Type(val code: String) {
        KEYBOARD_ANNOUNCEMENT("android11KeyboardAutofillArrived"),
        CALL_TO_UPGRADE("callToUpgradeAutofillPopupAndroid11")
    }

    enum class Confirm(val code: String) {
        DISPLAY("display"),
        OK("ok"),
        ACTIVATE_AUTOFILL("activate_autofill"),
        LATER("later")
    }
}