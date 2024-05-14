package com.dashlane.createaccount.pages.choosepassword

import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class CreateAccountChoosePasswordDataProvider @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator
) : BaseDataProvider<CreateAccountChoosePasswordContract.Presenter>(),
    CreateAccountChoosePasswordContract.DataProvider {

    lateinit var username: String
    var isB2B: Boolean = false

    override suspend fun validatePassword(password: CharSequence) {
        val passwordStrengthDeferred = getPasswordStrengthAsync(password)
        val passwordStrength = passwordStrengthDeferred.await()
        if (passwordStrength != null && passwordStrength.score >= MIN_PASSWORD_STRENGTH_SCORE) {
            presenter.notifySuccess(username, password.encodeUtf8ToObfuscated())
        } else {
            if (password.isEmpty()) {
                presenter.notifyPasswordEmpty(passwordStrength)
            } else {
                presenter.notifyPasswordInsufficient(passwordStrength)
            }
        }
    }

    override fun isPasswordlessEnabled(): Boolean = !isB2B

    override fun getPasswordStrengthAsync(password: CharSequence): Deferred<PasswordStrength?> =
        applicationCoroutineScope.async {
            try {
                passwordStrengthEvaluator.getPasswordStrength(password.toString())
            } catch (e: Exception) {
                null
            }
        }

    companion object {
        private val MIN_PASSWORD_STRENGTH_SCORE = PasswordStrengthScore.SAFELY_UNGUESSABLE
    }
}
