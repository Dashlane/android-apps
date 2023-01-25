package com.dashlane.auditduplicates.grouping

interface CalculateDuplicatesGroups {
    

    fun calculateGroups(accounts: List<AuthentifiantForGrouping>): Pair<GroupHeader, List<GroupDetail>>
}