package com.dashlane.item.v3.util

import com.dashlane.item.v3.util.SensitiveField.PASSWORD
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class SensitiveFieldLoader @Inject constructor(private val vaultDataQuery: VaultDataQuery) {

    fun getSensitiveField(id: String, field: SensitiveField): SyncObfuscatedValue? = when (field) {
        PASSWORD -> credentialPassword(id)
    }

    private fun credentialPassword(id: String) =
        (loadVaultItem(id)?.syncObject as? SyncObject.Authentifiant)?.password

    private fun loadVaultItem(id: String) =
        vaultDataQuery.queryLegacy(vaultFilter { specificUid(id) })
}