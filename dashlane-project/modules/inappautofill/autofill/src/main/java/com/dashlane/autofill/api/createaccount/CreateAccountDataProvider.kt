package com.dashlane.autofill.api.createaccount

import android.content.Context
import com.dashlane.autofill.api.common.GeneratePasswordDataProvider
import com.dashlane.autofill.api.common.domain.AutofillGeneratePasswordService
import com.dashlane.autofill.api.createaccount.domain.AutofillCreateAccountService
import com.dashlane.autofill.api.createaccount.domain.CredentialInfo
import com.dashlane.ext.application.KnownApplication
import com.dashlane.util.PackageUtilities
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.formatTitle
import com.dashlane.xml.domain.SyncObject



class CreateAccountDataProvider(
    generatorService: AutofillGeneratePasswordService,
    private val service: AutofillCreateAccountService
) : GeneratePasswordDataProvider<CreateAccountPresenter>(generatorService), CreateAccountContract.DataProvider {

    override suspend fun saveCredentialToVault(credential: CredentialInfo): VaultItem<SyncObject.Authentifiant>? =
        service.saveNewAuthentifiant(credential)

    override fun getMatchingWebsite(packageName: String) = KnownApplication.getPrimaryWebsite(packageName)

    override fun getCredentialTitle(
        context: Context?,
        website: String?,
        packageName: String?,
        inputWebsite: String?
    ): String? {
        if (website.isSemanticallyNull() && packageName != null && context != null) {
            val appName = PackageUtilities.getApplicationNameFromPackage(context, packageName)
            if (appName != null) return appName
        }
        return if (inputWebsite.isSemanticallyNull()) {
            SyncObject.Authentifiant.formatTitle(website)
        } else {
            SyncObject.Authentifiant.formatTitle(inputWebsite)
        }
    }
}
