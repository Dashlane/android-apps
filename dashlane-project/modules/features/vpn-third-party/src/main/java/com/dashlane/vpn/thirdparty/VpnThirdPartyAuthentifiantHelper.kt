package com.dashlane.vpn.thirdparty

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

interface VpnThirdPartyAuthentifiantHelper {
    

    fun newAuthentifiant(
        title: String,
        deprecatedUrl: String,
        email: String,
        password: SyncObfuscatedValue
    ): VaultItem<SyncObject.Authentifiant>

    

    suspend fun addAuthentifiant(authentifiant: VaultItem<SyncObject.Authentifiant>)
}