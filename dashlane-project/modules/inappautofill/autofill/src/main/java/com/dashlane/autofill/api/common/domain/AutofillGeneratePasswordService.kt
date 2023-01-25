package com.dashlane.autofill.api.common.domain

import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.passwordstrength.PasswordStrengthScore



interface AutofillGeneratePasswordService {

    

    suspend fun generatePassword(configuration: PasswordGeneratorCriteria): PasswordGeneratorWrapper.Result

    

    suspend fun evaluatePassword(password: String): PasswordStrengthScore?

    

    fun getPasswordGeneratorDefaultCriteria(): PasswordGeneratorCriteria

    

    fun setPasswordGeneratorDefaultCriteria(criteria: PasswordGeneratorCriteria)

    

    suspend fun saveToPasswordHistory(password: String, itemDomain: String, itemUid: String)
}