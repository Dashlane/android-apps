package com.dashlane.auditduplicates

import com.dashlane.auditduplicates.grouping.GroupDetail
import com.dashlane.auditduplicates.grouping.GroupHeader

interface AuditDuplicatesLogger {
    fun logAuditResults(calculatedGroups: Pair<GroupHeader, List<GroupDetail>>)
}