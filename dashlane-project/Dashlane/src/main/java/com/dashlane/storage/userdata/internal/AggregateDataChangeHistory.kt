package com.dashlane.storage.userdata.internal



data class AggregateDataChangeHistory(
    val dataChangeHistory: DataChangeHistoryForDb,
    val changeSetList: List<ChangeSetForDb>,
    val changeSetChangeList: List<ChangeSetChangeForDb>
)
