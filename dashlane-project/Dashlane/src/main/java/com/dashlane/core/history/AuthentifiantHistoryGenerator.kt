package com.dashlane.core.history

import com.dashlane.exception.NotLoggedInException
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.vault.history.toChangeSet
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject



class AuthentifiantHistoryGenerator @Inject constructor(
    private val sessionManager: SessionManager,
    private val provider: DataChangeHistoryQueryProvider
) : AbstractHistoryGenerator<SyncObject.Authentifiant>() {

    override val query: DataChangeHistoryQuery
        get() = provider.get()

    override fun newChangeSet(
        oldItem: VaultItem<SyncObject.Authentifiant>?,
        newItem: VaultItem<SyncObject.Authentifiant>
    ): SyncObject.DataChangeHistory.ChangeSet {
        val session = sessionManager.session ?: throw NotLoggedInException()
        return newItem.toChangeSet(oldItem?.syncObject, userName = session.userId)
    }
}