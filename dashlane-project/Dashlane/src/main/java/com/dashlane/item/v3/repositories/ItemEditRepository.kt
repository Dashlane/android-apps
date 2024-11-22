package com.dashlane.item.v3.repositories

import com.dashlane.authenticator.Hotp
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface ItemEditRepository {
    suspend fun save(initialVault: VaultItem<SyncObject>, data: Data<out FormData>): String
    suspend fun updateOtp(vaultItem: VaultItem<SyncObject.Authentifiant>, hotp: Hotp)
    suspend fun getPasswordReusedCount(password: String): Int
    fun getTeamspace(spaceId: String?): TeamSpace?
    fun hasPasswordHistory(authentifiant: VaultItem<SyncObject.Authentifiant>): Boolean
    fun remove2FAToken(vaultItem: VaultItem<*>, isProSpace: Boolean)
    suspend fun setItemViewed(vaultItem: VaultItem<SyncObject>)
}