package com.dashlane.autofill.api

import com.dashlane.autofill.api.common.domain.AutofillGeneratePasswordService
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.passwordgenerator.criteria
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.getPasswordStrengthScore
import javax.inject.Inject



class AutofillGeneratePasswordServiceImpl @Inject constructor(
    private val passwordGenerator: PasswordGeneratorWrapper,
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator
) : AutofillGeneratePasswordService {

    override suspend fun generatePassword(configuration: PasswordGeneratorCriteria) =
        passwordGenerator.generatePassword(configuration)

    override suspend fun evaluatePassword(password: String) =
        runCatching { passwordStrengthEvaluator.getPasswordStrengthScore(password) }.getOrNull()

    override fun getPasswordGeneratorDefaultCriteria() =
        passwordGenerator.criteria

    override fun setPasswordGeneratorDefaultCriteria(criteria: PasswordGeneratorCriteria) {
        passwordGenerator.criteria = criteria
    }

    override suspend fun saveToPasswordHistory(password: String, itemDomain: String, itemUid: String) {
        passwordGenerator.saveToPasswordHistory(password, itemDomain, itemUid)
    }
}