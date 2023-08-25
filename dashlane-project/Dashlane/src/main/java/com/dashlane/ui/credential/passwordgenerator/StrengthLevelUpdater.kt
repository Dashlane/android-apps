package com.dashlane.ui.credential.passwordgenerator

import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class StrengthLevelUpdater(private val coroutineScope: CoroutineScope) {

    private var lastJob: Job? = null

    fun updateWith(
        passwordEvaluator: PasswordStrengthEvaluator,
        password: String,
        action: (PasswordStrength) -> Unit
    ) {
        lastJob?.cancel()
        lastJob = coroutineScope.launch(Dispatchers.Main) {
            runCatching { passwordEvaluator.getPasswordStrength(password) }.onSuccess { action.invoke(it) }
        }
    }
}