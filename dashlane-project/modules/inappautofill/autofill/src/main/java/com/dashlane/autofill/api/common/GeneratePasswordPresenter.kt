package com.dashlane.autofill.api.common

import android.content.Context
import com.dashlane.autofill.api.R
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.borderColorRes
import com.dashlane.passwordstrength.getShortTitle
import com.dashlane.passwordstrength.isSafeEnoughForSpecialMode
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.useractivity.hermes.TrackingLogUtils
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class GeneratePasswordPresenter<P : GeneratePasswordContract.DataProvider, V : GeneratePasswordContract.ViewProxy>(
    private val coroutineScope: CoroutineScope,
    private val logger: AutofillGeneratePasswordLogger,
    website: String?,
    packageName: String?,
    private val userFeaturesChecker: UserFeaturesChecker
) : BasePresenter<P, V>(),
    GeneratePasswordContract.Presenter {

    var lastGeneratedPassword: String? = null

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val generatePasswordActor =
        coroutineScope.actor<PasswordGeneratorCriteria>(coroutineScope.coroutineContext, Channel.CONFLATED) {
            for (command in channel) {
                generate(command)
            }
        }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val evaluatePasswordActor =
        coroutineScope.actor<String>(coroutineScope.coroutineContext, Channel.CONFLATED) {
            for (command in channel) {
                evaluatePassword(command)
            }
        }

    private val domainForLogs: Domain =
        TrackingLogUtils.createDomainForLog(website, packageName)

    override fun onGenerateButtonClicked(criteria: PasswordGeneratorCriteria) {
        logger.logRefreshPassword()
        generatePasswordActor.trySend(criteria)
    }

    override fun generatePassword(criteria: PasswordGeneratorCriteria) {
        generatePasswordActor.trySend(criteria)
    }

    private suspend fun generate(criteria: PasswordGeneratorCriteria) {
        val result = provider.generatePassword(criteria)
        lastGeneratedPassword = result.password
        view.setPasswordField(result.password)
        logger.logGeneratePassword(criteria, domainForLogs)
    }

    private suspend fun evaluatePassword(password: String) {
        coroutineScope.launch(Dispatchers.Main) {
            val strengthScore = if (password.isNotSemanticallyNull()) {
                withContext(Dispatchers.IO) {
                    provider.evaluatePassword(password)
                }
            } else {
                null
            }
            val contextRef = context ?: return@launch
            updatePasswordStrengthAndColors(strengthScore, contextRef)
        }
    }

    override fun onPasswordUpdated(password: String) {
        evaluatePasswordActor.trySend(password)
    }

    private fun updatePasswordStrengthAndColors(
        strengthScore: PasswordStrengthScore?,
        contextRef: Context
    ) {
        val title = strengthScore?.getShortTitle(contextRef)
        val borderColor = contextRef.getColor(strengthScore?.borderColorRes ?: R.color.border_brand_standard_idle)
        val strength = strengthScore?.percentValue ?: 0
        val safeEnoughForSpecialMode = strengthScore?.isSafeEnoughForSpecialMode ?: false
        view.setPasswordStrength(
            title = title,
            color = borderColor,
            strength = strength,
            safeEnoughForSpecialMode = safeEnoughForSpecialMode
        )
    }

    override fun onGeneratorConfigurationChanged(criteria: PasswordGeneratorCriteria) {
        generatePasswordActor.trySend(criteria)
        provider.setPasswordGeneratorDefaultConfiguration(criteria)
    }

    override suspend fun saveGeneratedPasswordIfUsed(result: VaultItem<SyncObject.Authentifiant>) {
        val authentifiant = result.syncObject
        val password = authentifiant.password.toString()
        if (authentifiant.password?.equalsString(lastGeneratedPassword) == true) {
            val domain = authentifiant.urlForGoToWebsite?.toUrlOrNull()?.root ?: return
            provider.saveToPasswordHistory(password, domain, result.uid)
        }
    }

    override fun initSpecialMode() {
        val eligibleToSpecialMode = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.SPECIAL_PRIDE_MODE)
        view.initSpecialMode(eligibleToSpecialMode)
    }
}
