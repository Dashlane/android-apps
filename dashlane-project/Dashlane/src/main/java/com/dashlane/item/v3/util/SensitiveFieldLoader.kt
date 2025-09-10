package com.dashlane.item.v3.util

import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class SensitiveFieldLoader @Inject constructor(private val vaultDataQuery: VaultDataQuery) {
    suspend fun getSensitiveField(id: String, field: SensitiveField): SyncObfuscatedValue? =
        when (field) {
            SensitiveField.PASSWORD ->
                loadSyncObject<SyncObject.Authentifiant>(id)?.password
            else -> null
        }

    private suspend inline fun <reified T : SyncObject> loadSyncObject(id: String): T? =
        vaultDataQuery.query(vaultFilter { specificUid(id) })?.syncObject as T?
}