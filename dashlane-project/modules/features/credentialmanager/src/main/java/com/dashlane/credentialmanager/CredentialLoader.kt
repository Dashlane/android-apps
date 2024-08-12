package com.dashlane.credentialmanager

import com.dashlane.credentialmanager.model.PasskeyRequestOptions
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

interface CredentialLoader {
    fun loadPasswordCredentials(packageName: String): List<SummaryObject.Authentifiant>

    fun loadPasskeyCredentials(
        rpId: String,
        allowedCredentials: List<PasskeyRequestOptions.AllowCredentials>
    ): List<SummaryObject.Passkey>

    fun loadSyncObject(itemId: String): VaultItem<SyncObject>?

    fun countPasskeys(): Int

    fun countPasswords(): Int
}

class CredentialLoaderImpl @Inject constructor(
    private val credentialDataQuery: CredentialDataQuery,
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
) : CredentialLoader {
    override fun loadPasswordCredentials(packageName: String): List<SummaryObject.Authentifiant> {
        val dataQuery = credentialDataQuery
        val filter = dataQuery.createFilter().also {
            it.ignoreUserLock()
            it.packageName = packageName
            it.allowSimilarDomains = true
        }
        return dataQuery.queryAll(filter)
    }

    override fun loadPasskeyCredentials(
        rpId: String,
        allowedCredentials: List<PasskeyRequestOptions.AllowCredentials>
    ): List<SummaryObject.Passkey> {
        return genericDataQuery.queryAll(
            vaultFilter {
                ignoreUserLock()
                specificDataType(SyncObjectType.PASSKEY)
            }
        )
            .filterIsInstance(SummaryObject.Passkey::class.java)
            .filter { allowedCredentials.isEmpty() || it.credentialId in allowedCredentials.map { allowed -> allowed.id } }
            .filter { it.rpId == rpId }
    }

    override fun loadSyncObject(itemId: String): VaultItem<SyncObject>? {
        return vaultDataQuery.queryLegacy(
            vaultFilter {
                specificUid(itemId)
            }
        )
    }

    override fun countPasskeys(): Int {
        return genericDataQuery.queryAll(
            vaultFilter {
                ignoreUserLock()
                specificDataType(SyncObjectType.PASSKEY)
            }
        ).size
    }

    override fun countPasswords(): Int {
        return genericDataQuery.queryAll(
            vaultFilter {
                ignoreUserLock()
                specificDataType(SyncObjectType.AUTHENTIFIANT)
            }
        ).size
    }
}