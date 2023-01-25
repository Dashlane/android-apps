package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.filter.credentialFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject



class GeneratedPasswordQueryImpl @Inject constructor(
    private val dataStorageProvider: DataStorageProvider,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>
) : GeneratedPasswordQuery {

    private val genericDataQuery: GenericDataQuery
        get() = dataStorageProvider.genericDataQuery
    private val vaultDataQuery: VaultDataQuery
        get() = dataStorageProvider.vaultDataQuery
    private val credentialDataQuery: CredentialDataQuery
        get() = dataStorageProvider.credentialDataQuery

    override fun queryAllNotRevoked(): List<VaultItem<SyncObject.GeneratedPassword>> {
        val generatedPasswordIds = genericDataQuery.queryAll(
            genericFilter {
                specificDataType(SyncObjectType.GENERATED_PASSWORD)
            }
        ).map { it.id }

        
        val forbiddenDomains =
            teamspaceAccessorProvider.get()
                ?.revokedAndDeclinedSpaces
                ?.mapNotNull { space ->
                    space.domains.takeIf { space.isDomainRestrictionsEnable }
                }
                ?.flatten()

        if (forbiddenDomains.isNullOrEmpty()) {
            
            return getGeneratedPassword(generatedPasswordIds)
        }

        
        val credentialFilter = credentialFilter {
            forDomains(forbiddenDomains)
        }

        val forbiddenAuthentifiantId = credentialDataQuery
            .queryAll(credentialFilter)
            .map { it.id }

        val forbiddenPasswords = vaultDataQuery.queryAll(vaultFilter { specificUid(forbiddenAuthentifiantId) })
            .filterIsInstance<VaultItem<SyncObject.Authentifiant>>()
            .map { it.syncObject.password }

        return getGeneratedPassword(generatedPasswordIds).filterNot {
            it.syncObject.password in forbiddenPasswords
        }
    }

    private fun getGeneratedPassword(ids: List<String>): List<VaultItem<SyncObject.GeneratedPassword>> {
        val vaultFilter = vaultFilter {
            specificUid(ids)
        }

        return vaultDataQuery.queryAll(vaultFilter)
            .filterIsInstance<VaultItem<SyncObject.GeneratedPassword>>()
    }
}