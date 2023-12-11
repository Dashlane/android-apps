package com.dashlane.autofill.createaccount.view

import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountErrors
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

sealed class AutofillCreateAccountState {
    abstract val data: AutofillCreateAccountData

    data class Initial(
        override val data: AutofillCreateAccountData
    ) : AutofillCreateAccountState()

    data class InitSuggestions(
        override val data: AutofillCreateAccountData,
        val suggestionEmails: List<String>? = null,
        val suggestionLogins: List<String>? = null,
    ) : AutofillCreateAccountState()

    data class AccountCreated(
        override val data: AutofillCreateAccountData,
        val authentifiant: VaultItem<SyncObject.Authentifiant>
    ) : AutofillCreateAccountState()

    data class Cancelled(
        override val data: AutofillCreateAccountData
    ) : AutofillCreateAccountState()

    data class Error(
        override val data: AutofillCreateAccountData,
        val error: AutofillCreateAccountErrors
    ) : AutofillCreateAccountState()
}

data class AutofillCreateAccountData(
    val canSave: Boolean,
    val teamSpace: List<Teamspace>?
)