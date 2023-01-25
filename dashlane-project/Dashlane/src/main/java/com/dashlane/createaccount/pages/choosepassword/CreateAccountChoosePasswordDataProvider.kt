package com.dashlane.createaccount.pages.choosepassword

import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject

class CreateAccountChoosePasswordDataProvider @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    override val logger: CreateAccountChoosePasswordLogger,
) : BaseDataProvider<CreateAccountChoosePasswordContract.Presenter>(),
    CreateAccountChoosePasswordContract.DataProvider {

    lateinit var username: String

    override fun onShow() = logger.logLand()
    override fun onBack() = logger.logBack()

    override fun passwordVisibilityToggled(passwordShown: Boolean) =
        logger.logPasswordVisibilityToggle(passwordShown)

    override suspend fun validatePassword(password: CharSequence) {
        val passwordStrengthDeferred = getPasswordStrengthAsync(password)
        val passwordStrength = passwordStrengthDeferred.await()
        if (passwordStrength != null && passwordStrength.score >= MIN_PASSWORD_STRENGTH_SCORE) {
            logger.logPasswordChosen()
            presenter.notifySuccess(username, password.encodeUtf8ToObfuscated())
        } else {
            if (password.isEmpty()) {
                logger.logEmptyPassword()
                presenter.notifyPasswordEmpty(passwordStrength)
            } else {
                logger.logInsufficientPassword()
                presenter.notifyPasswordInsufficient(passwordStrength)
            }
        }
    }

    override fun getPasswordStrengthAsync(password: CharSequence): Deferred<PasswordStrength?> =
        globalCoroutineScope.async {
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
