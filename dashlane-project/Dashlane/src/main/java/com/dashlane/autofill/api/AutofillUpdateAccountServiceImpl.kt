package com.dashlane.autofill.api

import com.dashlane.autofill.api.changepassword.domain.AutofillUpdateAccountService
import com.dashlane.autofill.api.changepassword.domain.CredentialUpdateInfo
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.core.DataSync
import com.dashlane.useractivity.log.usage.UsageLogCode134
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject



class AutofillUpdateAccountServiceImpl @Inject constructor(private val autoFillDataBaseAccess: AutoFillDataBaseAccess) :
    AutofillUpdateAccountService {

    override suspend fun loadAuthentifiants(
        website: String?,
        packageName: String?
    ): List<SyncObject.Authentifiant> {
        val authByPackageName =
            if (packageName != null) {
                autoFillDataBaseAccess.loadAuthentifiantsByPackageName(packageName)
                    ?.mapNotNull { autoFillDataBaseAccess.loadAuthentifiant(it.id)?.syncObject } ?: emptyList()
            } else {
                emptyList()
            }
        val authByUrl = if (website != null) {
            autoFillDataBaseAccess.loadAuthentifiantsByUrl(website)
                ?.mapNotNull { autoFillDataBaseAccess.loadAuthentifiant(it.id)?.syncObject } ?: emptyList()
        } else {
            emptyList()
        }
        return (authByPackageName + authByUrl).distinctBy { it.id }
    }

    override suspend fun updateExistingAuthentifiant(credential: CredentialUpdateInfo): VaultItem<SyncObject.Authentifiant>? {
        val res = autoFillDataBaseAccess.updateAuthentifiantPassword(credential.id, credential.password)
        
        if (res != null) DataSync.sync(UsageLogCode134.Origin.SAVE)
        return res
    }
}