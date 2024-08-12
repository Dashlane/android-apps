package com.dashlane.ui.util

import com.dashlane.password.generator.PasswordGenerator
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.password.generator.generate
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.getPasswordStrengthScore
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PasswordGeneratorAndStrength @Inject constructor(
    private val passwordGenerator: PasswordGenerator,
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    @DefaultCoroutineDispatcher
    private val dispatcher: CoroutineDispatcher
) {
    suspend fun generate(criteria: PasswordGeneratorCriteria): PasswordGeneratorWrapper.Result =
        withContext(dispatcher) {
            val password = passwordGenerator.generate(criteria)
            val strength =
                runCatching { passwordStrengthEvaluator.getPasswordStrengthScore(password) }.getOrNull()
            PasswordGeneratorWrapper.Result(password, strength)
        }
}
