package com.dashlane.autofill.api.createaccount

import com.dashlane.autofill.api.common.AutofillGeneratePasswordLogger
import com.dashlane.autofill.api.common.GeneratePasswordPresenter
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountErrors
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountResultHandler
import com.dashlane.autofill.api.createaccount.domain.CredentialInfo
import com.dashlane.autofill.api.util.AutofillLogUtil
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.userfeatures.UserFeaturesChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateAccountPresenter(
    private val website: String?,
    private val packageName: String?,
    private val coroutineScope: CoroutineScope,
    private val logger: AutofillCreateAccountLogger,
    generateLogger: AutofillGeneratePasswordLogger,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    userFeaturesChecker: UserFeaturesChecker
) : CreateAccountContract.Presenter,
    GeneratePasswordPresenter<CreateAccountContract.DataProvider, CreateAccountContract.ViewProxy>(
        coroutineScope,
        generateLogger,
        website,
        packageName,
        userFeaturesChecker
    ) {

    private val resultHandler: AutofillCreateAccountResultHandler?
        get() = (activity as? AutofillCreateAccountResultHandler)

    override fun savedButtonClicked() {
        val userInput = view.getFilledData()
        if (!userInput.isComplete) {
            handleErrors(AutofillCreateAccountErrors.INCOMPLETE)
            return
        }

        coroutineScope.launch(mainDispatcher) {
            view.enableSave(false)
            val result = withContext(ioDispatcher) {
                val info = CredentialInfo(
                    provider.getCredentialTitle(context, website, packageName, userInput.website),
                    userInput.website,
                    userInput.login!!,
                    userInput.password!!,
                    packageName
                )
                provider.saveCredentialToVault(
                    info
                )
            }
            view.enableSave(true)
            if (result == null) {
                handleErrors(AutofillCreateAccountErrors.DATABASE_ERROR)
                return@launch
            }

            
            saveGeneratedPasswordIfUsed(result)

            logger.logSave(
                domainWrapper = AutofillLogUtil.extractDomainFrom(
                    urlDomain = website?.toUrlDomainOrNull(),
                    packageName = packageName
                ),
                credential = result
            )

            resultHandler?.onFinishWithResult(result)
        }
    }

    override fun initDialog() {
        prefillWebsiteIfPossible()
    }

    override fun onCancel() {
        val domainWrapper = AutofillLogUtil.extractDomainFrom(
            urlDomain = website?.toUrlDomainOrNull(),
            packageName = packageName
        )
        logger.onCancel(domainWrapper)
        resultHandler?.onCancel()
    }

    private fun prefillWebsiteIfPossible() {
        val result = getContentForWebsiteField()
        if (result != null) {
            view.prefillWebsiteFieldAndFocusOnLogin(result)
        }
    }

    private fun getContentForWebsiteField(): String? {
        return when {
            website.isNotSemanticallyNull() -> website
            packageName != null -> {
                provider.getMatchingWebsite(packageName)
            }
            else -> {
                null
            }
        }
    }

    private fun handleErrors(error: AutofillCreateAccountErrors) {
        when (error) {
            AutofillCreateAccountErrors.USER_LOGGED_OUT,
            AutofillCreateAccountErrors.DATABASE_ERROR -> {
                resultHandler?.onError(error)
            }
            AutofillCreateAccountErrors.INCOMPLETE -> {
                view.displayError(error.message)
            }
        }
    }
}
