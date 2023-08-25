package com.dashlane.vault

import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.collectionFilter
import com.dashlane.storage.userdata.accessor.queryAll
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject
import kotlin.math.roundToInt

class CollectionsReportProvider @Inject constructor(
    mainDataAccessor: MainDataAccessor
) {
    private val collectionDataQuery = mainDataAccessor.getCollectionDataQuery()

    fun computeCollectionTotalCount(teamspace: Teamspace): Int = collectionDataQuery.count(
        collectionFilter {
        ignoreUserLock()
        specificSpace(teamspace)
    }
    )

    fun computeItemPerCollectionAverageCount(teamspace: Teamspace): Int =
        computeItemPerCollectionAverageCount(queryAllCollectionsForSpace(teamspace))

    fun computeCollectionPerItemAverageCount(teamspace: Teamspace): Int =
        computeCollectionPerItemAverageCount(queryAllCollectionsForSpace(teamspace))

    fun computeCollectionsWithLoginCount(teamspace: Teamspace): Int =
        computeCollectionsWithLoginCount(queryAllCollectionsForSpace(teamspace))

    fun computeLoginsWithSingleCollectionCount(teamspace: Teamspace): Int =
        computeLoginsWithSingleCollectionCount(queryAllCollectionsForSpace(teamspace))

    fun computeLoginsWithMultipleCollectionsCount(teamspace: Teamspace): Int =
        computeLoginsWithMultipleCollectionCount(queryAllCollectionsForSpace(teamspace))

    private fun queryAllCollectionsForSpace(teamspace: Teamspace) = collectionDataQuery.queryAll {
        collectionFilter {
            ignoreUserLock()
            specificSpace(teamspace)
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