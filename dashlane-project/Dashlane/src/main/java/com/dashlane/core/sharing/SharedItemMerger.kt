package com.dashlane.core.sharing

import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.XmlData
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.objectType
import com.dashlane.xml.domain.properties.AuthentifiantXml
import com.dashlane.xml.domain.properties.SecureNoteXml
import com.dashlane.xml.domain.properties.SyncObjectXml

@OptIn(ExperimentalStdlibApi::class)
object SharedItemMerger {

    private val SHARED_FIELDS_AUTHENTIFIANT = arrayListOf(
        AuthentifiantXml.EMAIL,
        AuthentifiantXml.LOGIN,
        AuthentifiantXml.PASSWORD,
        AuthentifiantXml.SECONDARY_LOGIN,
        AuthentifiantXml.NOTE,
        AuthentifiantXml.OTP_SECRET,
        AuthentifiantXml.OTP_URL,
        AuthentifiantXml.URL,
        AuthentifiantXml.USER_SELECTED_URL,
        AuthentifiantXml.USE_FIXED_URL,
        AuthentifiantXml.TITLE,
        AuthentifiantXml.LINKED_SERVICES,
        SyncObjectXml.ATTACHMENTS
    )

    private val SHARED_FIELDS_SECURE_NOTE = arrayListOf(
        SecureNoteXml.TITLE,
        SecureNoteXml.CONTENT,
        SecureNoteXml.SECURED,
        SyncObjectXml.ATTACHMENTS
    )

    fun needSharingMerge(original: VaultItem<*>?, newItem: VaultItem<*>?): Boolean {
        return newItem?.sharingPermission != null || original?.sharingPermission != null
    }

    fun mergeVaultItemToSave(
        oldVaultItem: VaultItem<*>?,
        newVaultItem: VaultItem<*>?,
        origin: DataSaver.SaveRequest.Origin
    ): VaultItem<*>? {
        if (newVaultItem == null) {
            return null
        }
        if (oldVaultItem == null) {
            return newVaultItem.copyWithAttrs { syncState = SyncState.MODIFIED }
        }
        var mergedVaultItem = newVaultItem
        if (origin !== DataSaver.SaveRequest.Origin.SHARING_SYNC) {
            mergedVaultItem = newVaultItem.copy(
                hasDirtySharedField = oldVaultItem.hasDirtySharedField
            )
        }

        when (origin) {
            
            
            DataSaver.SaveRequest.Origin.SHARING_SYNC -> {
                val newSharedMap: Map<String, XmlData> = newVaultItem.syncObject.filterSharedFields()
                val syncObject = oldVaultItem.syncObject.copyWithMap(newSharedMap)
                mergedVaultItem = oldVaultItem.copyWithAttrs { sharingPermission = newVaultItem.sharingPermission }
                    .copy(syncObject = syncObject)
            }
            
            
            DataSaver.SaveRequest.Origin.PERSONAL_SYNC -> {
                val oldSharedMap: Map<String, XmlData> = oldVaultItem.syncObject.filterSharedFields()
                val syncObject = newVaultItem.syncObject.copyWithMap(oldSharedMap)
                mergedVaultItem = oldVaultItem.copy(syncObject = syncObject)
            }
            
            
            DataSaver.SaveRequest.Origin.LOCAL_EDIT -> {
                val newSharedMap: Map<String, XmlData> = newVaultItem.syncObject.filterSharedFields()
                val oldSharedMap: Map<String, XmlData> = oldVaultItem.syncObject.filterSharedFields()
                mergedVaultItem = if (newSharedMap != oldSharedMap) {
                    newVaultItem.copyWithAttrs { hasDirtySharedField = true }
                } else {
                    newVaultItem
                }
            }
        }

        mergedVaultItem = mergedVaultItem.markedAsSynced(origin)
        return mergedVaultItem
    }

    private fun SyncObject.copyWithMap(map: Map<String, XmlData>) =
        (data + map).toSyncObject(objectType)

    private fun SyncObject.filterSharedFields(): Map<String, XmlData> {
        return when (objectType) {
            SyncObjectType.AUTHENTIFIANT -> data.filterKeys { it in SHARED_FIELDS_AUTHENTIFIANT }
            SyncObjectType.SECURE_NOTE -> data.filterKeys { it in SHARED_FIELDS_SECURE_NOTE }
            else -> error("Unsupported types")
        }.toSortedMap()
    }

    private fun Map<String, XmlData>.toSyncObject(type: SyncObjectType) = type.create(this)

    private fun VaultItem<*>.markedAsSynced(origin: DataSaver.SaveRequest.Origin): VaultItem<*> {
        return when {
            origin === DataSaver.SaveRequest.Origin.PERSONAL_SYNC -> {
                copyWithAttrs { syncState = SyncState.SYNCED }
            }
            origin === DataSaver.SaveRequest.Origin.SHARING_SYNC -> {
                copyWithAttrs { hasDirtySharedField = false }
            }
            else -> this
        }
    }
}
