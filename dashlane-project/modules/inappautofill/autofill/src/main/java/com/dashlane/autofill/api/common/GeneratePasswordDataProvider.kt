package com.dashlane.autofill.api.common

import com.dashlane.autofill.api.common.domain.AutofillGeneratePasswordService
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.skocken.presentation.provider.BaseDataProvider

abstract class GeneratePasswordDataProvider<T : GeneratePasswordContract.Presenter>(private val service: AutofillGeneratePasswordService) :
    BaseDataProvider<T>(), GeneratePasswordContract.DataProvider {

    override suspend fun generatePassword(criteria: PasswordGeneratorCriteria) =
        service.generatePassword(criteria)

    override suspend fun evaluatePassword(password: String) = service.evaluatePassword(password)

    override fun getPasswordGeneratorDefaultConfiguration() = service.getPasswordGeneratorDefaultCriteria()

    override fun setPasswordGeneratorDefaultConfiguration(criteria: PasswordGeneratorCriteria) =
        service.setPasswordGeneratorDefaultCriteria(criteria)

    override suspend fun saveToPasswordHistory(password: String, itemDomain: String, itemUid: String) =
        service.saveToPasswordHistory(password, itemDomain, itemUid)
}
