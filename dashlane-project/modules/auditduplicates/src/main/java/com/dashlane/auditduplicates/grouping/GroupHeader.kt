package com.dashlane.auditduplicates.grouping



data class GroupHeader(
    val totalNbCredentials: Int,
    val totalNbDuplicates: Int,
    val totalNbDuplicateGroups: Int
)