package com.dashlane.autofill.api.changepassword

import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors.DATABASE_ERROR
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors.INCOMPLETE
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors.NO_MATCHING_CREDENTIAL
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordErrors.USER_LOGGED_OUT
import com.dashlane.autofill.api.changepassword.domain.AutofillChangePasswordResultHandler
import com.dashlane.autofill.api.changepassword.domain.CredentialUpdateInfo
import com.dashlane.autofill.api.common.AutofillGeneratePasswordLogger
import com.dashlane.autofill.api.common.GeneratePasswordPresenter
import com.dashlane.util.userfeatures.UserFeaturesChecker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class ChangePasswordPresenter(
    private val coroutineScope: CoroutineScope,
    private val logger: AutofillChangePasswordLogger,
    generateLogger: AutofillGeneratePasswordLogger,
    website: String?,
    packageName: String?,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    userFeaturesChecker: UserFeaturesChecker
) : ChangePasswordContract.Presenter,
    GeneratePasswordPresenter<ChangePasswordContract.DataProvider, ChangePasswordContract.ViewProxy>(
        coroutineScope,
        generateLogger,
        website,
        packageName,
        userFeaturesChecker
    ) {

    private val resultHandler: AutofillChangePasswordResultHandler?
        get() = (activity as? AutofillChangePasswordResultHandler)
    private var defaultCredentialId: String = ""

    override fun useButtonClicked() {
        val userInput = view.getFilledData()
        if (!userInput.isComplete) {
            handleErrors(INCOMPLETE)
            return
        }
        coroutineScope.launch(mainDispatcher) {
            view.enableUse(false)
            val item = provider.getCredential(userInput.login)
            val result = withContext(ioDispatcher) {
                val info = CredentialUpdateInfo(
                    item.id!!,
                    userInput.password!!
                )
                provider.updateCredentialToVault(info)
            }
            view.enableUse(true)
            if (result == null) {
                handleErrors(DATABASE_ERROR)
                return@launch
            }

            
            saveGeneratedPasswordIfUsed(result)

            logger.logUpdate(result.uid)
            resultHandler?.onFinishWithResult(result, item)
        }
    }

    override fun onCancel() {
        resultHandler?.onCancel()
        logger.logCancel(defaultCredentialId)
    }

    override fun initDialog() {
        prefillLogin()
    }

    

    private fun prefillLogin() {
        coroutineScope.launch(Dispatchers.Main) {
            val existingAuthentifiants = provider.loadAuthentifiants()
            val logins = existingAuthentifiants.mapNotNull { it.login ?: it.email }
            if (existingAuthentifiants.isEmpty()) {
                handleErrors(NO_MATCHING_CREDENTIAL)
                return@launch
            }
            view.prefillLogin(logins)
            val filledData = view.getFilledData()
            defaultCredentialId = existingAuthentifiants.first {
                it.login == filledData.login || it.email == filledData.login
            }.id ?: ""
            logger.logOnClickUpdateAccount(defaultCredentialId)
        }
    }

    private fun handleErrors(error: AutofillChangePasswordErrors) {
        when (error) {
            USER_LOGGED_OUT,
            DATABASE_ERROR,
            NO_MATCHING_CREDENTIAL -> {
                resultHandler?.onError(error)
            }
            INCOMPLETE -> activity?.let { view.displayError(it.getString(error.resId)) }
        }
    }
}
