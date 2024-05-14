package com.dashlane.autofill.generatepassword

import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.passwordstrength.PasswordStrengthScore

class GeneratePasswordContract {
    interface ViewProxy {

        fun setPasswordField(newPassword: String)

        fun setPasswordStrength(title: String?, color: Int, strength: Int, safeEnoughForSpecialMode: Boolean)

        fun updateSpecialMode(specialMode: GeneratePasswordSpecialMode?)
    }

    interface DataProvider {
        suspend fun generatePassword(criteria: PasswordGeneratorCriteria): PasswordGeneratorWrapper.Result

        suspend fun evaluatePassword(password: String): PasswordStrengthScore?

        fun getPasswordGeneratorDefaultConfiguration(): PasswordGeneratorCriteria

        fun setPasswordGeneratorDefaultConfiguration(criteria: PasswordGeneratorCriteria)

        suspend fun saveToPasswordHistory(password: String, itemDomain: String, itemUid: String)
    }
}