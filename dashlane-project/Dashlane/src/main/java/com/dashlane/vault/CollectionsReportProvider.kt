package com.dashlane.vault

import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.filter.collectionFilter
import com.dashlane.storage.userdata.accessor.queryAllLegacy
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject
import kotlin.math.roundToInt

class CollectionsReportProvider @Inject constructor(
    private val collectionDataQuery: CollectionDataQuery
) {
    fun computeCollectionTotalCount(teamSpace: TeamSpace): Int = collectionDataQuery.count(
        collectionFilter {
            ignoreUserLock()
            specificSpace(teamSpace)
        }
    )

    fun computeItemPerCollectionAverageCount(teamSpace: TeamSpace): Int =
        computeItemPerCollectionAverageCount(queryAllCollectionsForSpace(teamSpace))

    fun computeCollectionPerItemAverageCount(teamSpace: TeamSpace): Int =
        computeCollectionPerItemAverageCount(queryAllCollectionsForSpace(teamSpace))

    fun computeCollectionsWithLoginCount(teamSpace: TeamSpace): Int =
        computeCollectionsWithLoginCount(queryAllCollectionsForSpace(teamSpace))

    fun computeLoginsWithSingleCollectionCount(teamSpace: TeamSpace): Int =
        computeLoginsWithSingleCollectionCount(queryAllCollectionsForSpace(teamSpace))

    fun computeLoginsWithMultipleCollectionsCount(teamSpace: TeamSpace): Int =
        computeLoginsWithMultipleCollectionCount(queryAllCollectionsForSpace(teamSpace))

    private fun queryAllCollectionsForSpace(teamSpace: TeamSpace) = collectionDataQuery.queryAllLegacy {
        collectionFilter {
            ignoreUserLock()
            specificSpace(teamSpace)
        }
    }
}

internal fun computeItemPerCollectionAverageCount(list: List<SummaryObject.Collection>): Int =
    list.map {
        it.vaultItems?.distinct()?.size ?: 0
    }
        .filter { it > 0 }
        .average()
        .takeIf { it.isFinite() }
        ?.roundToInt() ?: 0

internal fun computeCollectionPerItemAverageCount(list: List<SummaryObject.Collection>): Int =
    list.mapNotNull {
        it.vaultItems?.distinct()
    }
        .flatten()
        .groupingBy { it.id }
        .eachCount()
        .map { it.value }
        .average()
        .takeIf { it.isFinite() }
        ?.roundToInt() ?: 0

internal fun computeCollectionsWithLoginCount(list: List<SummaryObject.Collection>): Int =
    list.filter { collection ->
        collection.vaultItems?.map { it.type }?.contains(SyncObject.CollectionDataType.KWAUTHENTIFIANT) ?: false
    }.size

internal fun computeCollectionCountByLogin(list: List<SummaryObject.Collection>) = list.mapNotNull { collection ->
    collection.vaultItems?.filter {
        it.type == SyncObject.CollectionDataType.KWAUTHENTIFIANT
    }?.distinct()
}
    .flatten()
    .groupingBy { it.id }
    .eachCount()

internal fun computeLoginsWithSingleCollectionCount(list: List<SummaryObject.Collection>) =
    computeCollectionCountByLogin(list).filter { it.value == 1 }.size

internal fun computeLoginsWithMultipleCollectionCount(list: List<SummaryObject.Collection>) =
    computeCollectionCountByLogin(list).filter { it.value > 1 }.size