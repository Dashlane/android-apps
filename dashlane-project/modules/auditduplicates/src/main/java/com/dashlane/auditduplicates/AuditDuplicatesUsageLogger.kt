package com.dashlane.auditduplicates

import com.dashlane.auditduplicates.grouping.GroupDetail
import com.dashlane.auditduplicates.grouping.GroupHeader
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode141
import com.dashlane.useractivity.log.usage.UsageLogRepository
import java.util.UUID
import javax.inject.Inject



class AuditDuplicatesUsageLogger @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : AuditDuplicatesLogger {

    override fun logAuditResults(calculatedGroups: Pair<GroupHeader, List<GroupDetail>>) {
        
        val checkId = UUID.randomUUID().toString().substring(0, 5)
        buildUsageLogList(checkId, calculatedGroups.first, calculatedGroups.second).forEach {
            log(it)
        }
    }

    private fun buildUsageLogList(
        checkId: String,
        groupHeader: GroupHeader,
        groupDetailList: List<GroupDetail>
    ): List<UsageLogCode141> {
        return groupDetailList.mapIndexed { index, groupDetail ->
            UsageLogCode141(
                checkId = checkId,
                totalNbCredentials = groupHeader.totalNbCredentials.toLong(),
                totalNbDuplicates = groupHeader.totalNbDuplicates.toLong(),
                totalNbDuplicateGroups = groupHeader.totalNbDuplicateGroups.toLong(),
                groupIndex = index.toLong(),
                groupNbCredentials = groupDetail.groupNbCredentials.toLong(),
                groupNbExactDuplicates = groupDetail.groupNbExactDuplicates.toLong(),
                groupNbDifferentHosts = groupDetail.groupNbDifferentHosts.toLong(),
                groupNbDifferentPasswords = groupDetail.groupNbDifferentPasswords.toLong()
            )
        }
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(log)
    }
}
