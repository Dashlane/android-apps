package com.dashlane.autofill.api.changepassword

import com.dashlane.autofill.api.changepassword.domain.AutofillUpdateAccountService
import com.dashlane.autofill.api.changepassword.domain.CredentialUpdateInfo
import com.dashlane.autofill.api.common.GeneratePasswordDataProvider
import com.dashlane.autofill.api.common.domain.AutofillGeneratePasswordService
import com.dashlane.xml.domain.SyncObject



class ChangePasswordDataProvider(
    private val website: String?,
    private val packageName: String?,
    private val service: AutofillUpdateAccountService,
    private val configuration: AutoFillChangePasswordConfiguration,
    generateService: AutofillGeneratePasswordService
) : GeneratePasswordDataProvider<ChangePasswordPresenter>(generateService), ChangePasswordContract.DataProvider {

    private lateinit var existingAuthentifiants: List<SyncObject.Authentifiant>

    override suspend fun loadAuthentifiants() = service.loadAuthentifiants(website, packageName).also {
        existingAuthentifiants = it
    }

    override suspend fun updateCredentialToVault(credential: CredentialUpdateInfo) =
        service.updateExistingAuthentifiant(credential)?.also { configuration.onItemUpdated.invoke() }

    override fun getCredential(login: String) = existingAuthentifiants.first { it.login == login || it.email == login }
}
