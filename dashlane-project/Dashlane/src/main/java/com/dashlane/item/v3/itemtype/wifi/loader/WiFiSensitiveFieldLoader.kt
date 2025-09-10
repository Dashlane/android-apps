package com.dashlane.item.v3.itemtype.wifi.loader

import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class WiFiSensitiveFieldLoader @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
) {
    suspend fun loadPassphrase(state: ItemEditState<WiFiFormData>): String? {
        val id = state.itemId ?: return null
        val currentFormData = state.datas?.current ?: return null

        return if (currentFormData.formData.passphrase != null) {
            currentFormData.formData.passphrase
        } else {
            val vaultItem = loadSyncObject(id) ?: return null

            vaultItem.syncObject.passphrase.toString()
                .takeIf { it.isNotSemanticallyNull() }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadSyncObject(id: String): VaultItem<SyncObject.WiFi>? =
        vaultDataQuery.query(vaultFilter { specificUid(id) })
            .takeIf { it is VaultItem<*> && it.syncObject is SyncObject.WiFi }
            ?.let { it as? VaultItem<SyncObject.WiFi> }
}