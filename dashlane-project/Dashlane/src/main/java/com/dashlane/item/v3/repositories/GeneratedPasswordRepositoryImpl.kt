package com.dashlane.item.v3.repositories

import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GeneratedPasswordQuery
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.addAuthId
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class GeneratedPasswordRepositoryImpl @Inject constructor(
    private val dataSaver: DataSaver,
    private val generatedPasswordQuery: GeneratedPasswordQuery
) : GeneratedPasswordRepository {
    override suspend fun updateGeneratedPassword(
        data: CredentialFormData,
        itemToSave: VaultItem<SyncObject>
    ) = data.password?.idFromPasswordGenerator?.let { id ->
        generatedPasswordQuery.queryAllNotRevoked()
            .firstOrNull { it.uid == id }
            ?.addAuthId(itemToSave.uid)
            ?.copyWithAttrs { syncState = SyncState.MODIFIED }
            ?.let { dataSaver.save(it) }
    } ?: false
}