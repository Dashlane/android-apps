package com.dashlane.teamspaces.db

import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoRestrictionSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.space.SpecificSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.TeamspaceMatcher
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class SmartSpaceCategorizationDatabaseAccessor @Inject constructor(
    private val dataSaver: DataSaver,
    private val vaultDataQuery: VaultDataQuery,
    private val genericDataQuery: GenericDataQuery
) {

    fun getSummaryCandidatesForCategorization(): List<SummaryObject> {
        val summaryItems = mutableListOf<SummaryObject>()
        TeamspaceMatcher.DATA_TYPE_TO_MATCH.forEach { dataType ->
            val filter = genericFilter {
                specificDataType(dataType)
                spaceFilter = NoRestrictionSpaceFilter
                allStatusFilter()
            }
            summaryItems.addAll(genericDataQuery.queryAll(filter))
        }
        return summaryItems
    }

    fun getSummaryItemsForSpace(pastTeam: TeamSpace.Business.Past): List<SummaryObject> {
        val filter = genericFilter {
            this.spaceFilter = SpecificSpaceFilter(spaces = listOf(pastTeam))
            allStatusFilter()
        }
        return genericDataQuery.queryAll(filter)
    }

    suspend fun getVaultItems(ids: List<String>): List<VaultItem<SyncObject>> {
        val vaultFilter = vaultFilter {
            specificUid(ids)
            spaceFilter = NoRestrictionSpaceFilter
            allStatusFilter()
        }
        return vaultDataQuery.queryAll(vaultFilter)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getGeneratedPasswords(): List<VaultItem<SyncObject.GeneratedPassword>> {
        val vaultFilter = vaultFilter {
            specificDataType(SyncObjectType.GENERATED_PASSWORD)
        }
        return vaultDataQuery.queryAll(vaultFilter).map { it as VaultItem<SyncObject.GeneratedPassword> }
    }

    suspend fun save(items: List<VaultItem<*>>) = dataSaver.save(items)
}