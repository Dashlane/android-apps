package com.dashlane.autofill.api

import android.content.Context
import com.dashlane.R
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountService
import com.dashlane.autofill.createaccount.domain.CredentialInfo
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.sync.DataSync
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.storage.userdata.EmailSuggestionProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AutofillCreateAccountServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val autoFillDataBaseAccess: AutoFillDataBaseAccess,
    private val emailSuggestionProvider: EmailSuggestionProvider,
    private val dataSync: DataSync,
    private val knownApplicationProvider: KnownApplicationProvider
) : AutofillCreateAccountService {
    override suspend fun saveNewAuthentifiant(credential: CredentialInfo): VaultItem<SyncObject.Authentifiant>? {
        val res = autoFillDataBaseAccess.createNewAuthentifiantFromAutofill(
            context,
            credential.title,
            credential.website,
            credential.login,
            credential.password,
            credential.packageName,
            credential.spaceId
        )
        
        if (res != null) dataSync.sync(Trigger.SAVE)
        return res
    }

    override fun loadExistingLogins() = emailSuggestionProvider.getAllEmails()

    override fun getFamousWebsitesList(): List<String> {
        return context.resources.getStringArray(R.array.websites_suggestions).toList()
    }

    override fun getWebsiteForPackage(packageName: String): String? =
        knownApplicationProvider.getKnownApplication(packageName)?.mainDomain
}