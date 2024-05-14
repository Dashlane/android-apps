package com.dashlane.autofill.createaccount

import android.content.Context
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountService
import com.dashlane.autofill.createaccount.domain.CredentialInfo
import com.dashlane.util.PackageUtilities
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.formatTitle
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class CreateAccountDataProvider @Inject constructor(
    private val service: AutofillCreateAccountService
) {

    suspend fun saveCredentialToVault(credential: CredentialInfo): VaultItem<SyncObject.Authentifiant>? =
        service.saveNewAuthentifiant(credential)

    fun getMatchingWebsite(packageName: String) = service.getWebsiteForPackage(packageName)

    fun getCredentialTitle(
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
