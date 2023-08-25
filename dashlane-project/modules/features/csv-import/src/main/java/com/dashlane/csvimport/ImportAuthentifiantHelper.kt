package com.dashlane.csvimport

import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

interface ImportAuthentifiantHelper {
    fun newAuthentifiant(
        linkedServices: SyncObject.Authentifiant.LinkedServices? = null,
        deprecatedUrl: String? = null,
        email: String? = "",
        login: String? = "",
        password: SyncObfuscatedValue? = null,
        title: String? = null,
        teamId: String? = null
    ): VaultItem<SyncObject.Authentifiant>

    suspend fun addAuthentifiants(
        authentifiants: List<VaultItem<SyncObject.Authentifiant>>,
        origin: UsageLogCode11.From
    ): Int
}