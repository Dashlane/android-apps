package com.dashlane.storage.userdata.accessor

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface DataSaver {

    

     suspend fun save(item: VaultItem<*>) = save(listOf(item))

    

    suspend fun save(items: List<VaultItem<*>>): Boolean {
        return save(
            SaveRequest(
                itemsToSave = items,
                origin = SaveRequest.Origin.LOCAL_EDIT,
                mode = SaveRequest.Mode.INSERT_OR_UPDATE
            )
        ).isNotEmpty()
    }

    

    suspend fun <T : SyncObject> save(saveRequest: SaveRequest<T>): List<VaultItem<*>>

    

    suspend fun forceDelete(item: VaultItem<*>): Boolean

    data class SaveRequest<T : SyncObject>(
        val itemsToSave: List<VaultItem<T>>,
        val origin: Origin = Origin.LOCAL_EDIT,
        val mode: Mode = Mode.INSERT_OR_UPDATE
    ) {
        enum class Mode {
            INSERT_ONLY, INSERT_OR_UPDATE
        }

        enum class Origin {
            PERSONAL_SYNC, SHARING_SYNC, LOCAL_EDIT
        }
    }
}