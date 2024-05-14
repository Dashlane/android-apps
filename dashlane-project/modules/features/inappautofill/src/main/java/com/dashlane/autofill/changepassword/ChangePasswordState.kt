package com.dashlane.autofill.changepassword

import com.dashlane.autofill.changepassword.domain.AutofillChangePasswordErrors
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

sealed class AutofillChangePasswordState {
    abstract val data: AutofillChangePasswordData

    data class Initial(
        override val data: AutofillChangePasswordData
    ) : AutofillChangePasswordState()

    data class PrefillLogin(
        override val data: AutofillChangePasswordData,
        val logins: List<String>
    ) : AutofillChangePasswordState()

    data class PasswordChanged(
        override val data: AutofillChangePasswordData,
        val authentifiant: VaultItem<SyncObject.Authentifiant>,
        val oldAuthentifiant: SyncObject.Authentifiant,
    ) : AutofillChangePasswordState()

    data class Cancelled(
        override val data: AutofillChangePasswordData
    ) : AutofillChangePasswordState()

    data class Error(
        override val data: AutofillChangePasswordData,
        val error: AutofillChangePasswordErrors
    ) : AutofillChangePasswordState()
}

data class AutofillChangePasswordData(val canUse: Boolean)