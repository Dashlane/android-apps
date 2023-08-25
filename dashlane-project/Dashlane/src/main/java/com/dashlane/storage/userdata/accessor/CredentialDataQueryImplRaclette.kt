package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.CredentialFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class CredentialDataQueryImplRaclette @Inject constructor(
    private val genericDataQuery: GenericDataQueryImplRaclette,
    private val vaultDataQueryImplNew: VaultDataQueryImplRaclette,
) : CredentialDataQuery {
    override fun queryAllPasswords(): List<String> {
        val ids = queryAll(createFilter()).filterNot {
            it.isPasswordEmpty
        }.map { it.id }
        return vaultDataQueryImplNew.queryAll(
            vaultFilter {
            specificUid(ids)
            specificDataType(SyncObjectType.AUTHENTIFIANT)
        }
        ).filterIsInstance<VaultItem<SyncObject.Authentifiant>>()
            .map { it.syncObject.password.toString() }
    }

    override fun queryAllUrls(): Set<String> {
        return queryAll(createFilter()).map {
            listOf(it.userSelectedUrl, it.url)
        }.flatten().filterNotNull().toSet()
    }

    override fun createFilter() = CredentialFilter()

    override fun queryFirst(filter: CredentialFilter): SummaryObject.Authentifiant? {
        return genericDataQuery.queryFirst(filter) as SummaryObject.Authentifiant?
    }

    @Suppress("UNCHECKED_CAST")
    override fun queryAll(filter: CredentialFilter): List<SummaryObject.Authentifiant> {
        return genericDataQuery.queryAll(filter) as List<SummaryObject.Authentifiant>
    }

    override fun count(filter: CredentialFilter) = genericDataQuery.count(filter)
}