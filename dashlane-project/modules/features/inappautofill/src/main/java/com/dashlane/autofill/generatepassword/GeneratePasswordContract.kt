package com.dashlane.autofill.generatepassword

class GeneratePasswordContract {
    interface ViewProxy {
        fun setPasswordField(newPassword: String)

        fun setPasswordStrength(title: String?, color: Int, strength: Int, safeEnoughForSpecialMode: Boolean)

        fun updateSpecialMode(specialMode: GeneratePasswordSpecialMode?)
    }
}