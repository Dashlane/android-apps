package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.CollectionFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.uid.SpecificUidFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class CollectionDataQueryImpl @Inject constructor(
    private val genericDataQuery: GenericDataQueryImplRaclette,
    private val vaultDataQuery: VaultDataQueryImplRaclette,
) : CollectionDataQuery {

    override fun queryByName(name: String, filter: CollectionFilter): VaultItem<SyncObject.Collection>? =
        queryFirst(filter.apply { this.name = name })?.let {
            vaultDataQuery.queryLegacy(
                vaultFilter {
                spaceFilter = filter
                dataTypeFilter = filter
                specificUid(it.id)
            }
            )?.asVaultItemOfClassOrNull(SyncObject.Collection::class.java)
        }

    override fun queryById(id: String): VaultItem<SyncObject.Collection>? =
        vaultDataQuery.queryLegacy(
            vaultFilter {
            val filter = createFilter()
            spaceFilter = filter
            dataTypeFilter = filter
            specificUid(id)
        }
        )?.asVaultItemOfClassOrNull(SyncObject.Collection::class.java)

    override fun queryByIds(ids: List<String>): List<VaultItem<SyncObject.Collection>> =
        vaultDataQuery.queryAllLegacy(
            vaultFilter {
                val filter = createFilter()
                spaceFilter = filter
                dataTypeFilter = filter
                specificUid(ids)
            }
        ).filterIsInstance<VaultItem<SyncObject.Collection>>()

    override fun queryVaultItemsWithCollectionId(id: String): List<SummaryObject> {
        val collection = queryById(id)
        return genericDataQuery.queryAll(
            genericFilter {
                uidFilter = SpecificUidFilter(collection?.syncObject?.vaultItems?.mapNotNull { it.id } ?: emptyList())
            }
        )
    }

    override fun createFilter() = CollectionFilter()

    override fun queryFirst(filter: CollectionFilter): SummaryObject.Collection? {
        return genericDataQuery.queryFirst(filter) as SummaryObject.Collection?
    }

    @Suppress("UNCHECKED_CAST")
    override fun queryAll(filter: CollectionFilter): List<SummaryObject.Collection> {
        return genericDataQuery.queryAll(filter) as List<SummaryObject.Collection>
    }

    override fun count(filter: CollectionFilter): Int = genericDataQuery.count(filter)
}