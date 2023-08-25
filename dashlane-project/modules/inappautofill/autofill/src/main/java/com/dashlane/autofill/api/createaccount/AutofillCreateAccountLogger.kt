package com.dashlane.autofill.api.createaccount

import com.dashlane.autofill.api.util.DomainWrapper
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface AutofillCreateAccountLogger {

    fun onCancel(domainWrapper: DomainWrapper)

    fun logSave(domainWrapper: DomainWrapper, credential: VaultItem<SyncObject.Authentifiant>)
}