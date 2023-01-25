package com.dashlane.autofill.api.createaccount

import android.content.Context
import com.dashlane.autofill.api.common.GeneratePasswordContract
import com.dashlane.autofill.api.createaccount.domain.CredentialInfo
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



class CreateAccountContract {

    interface Presenter : GeneratePasswordContract.Presenter {
        

        fun savedButtonClicked()

        

        fun initDialog()

        

        fun onCancel()
    }

    interface ViewProxy : GeneratePasswordContract.ViewProxy {
        

        fun getFilledData(): FilledData

        

        fun prefillWebsiteFieldAndFocusOnLogin(website: String)

        

        fun enableSave(enable: Boolean)

        

        fun displayError(message: String)
    }

    interface DataProvider : GeneratePasswordContract.DataProvider {
        

        suspend fun saveCredentialToVault(credential: CredentialInfo): VaultItem<SyncObject.Authentifiant>?

        

        fun getMatchingWebsite(packageName: String): String?

        

        fun getCredentialTitle(
            context: Context?,
            website: String?,
            packageName: String?,
            inputWebsite: String?
        ): String?
    }
}