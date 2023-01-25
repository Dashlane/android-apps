package com.dashlane.auditduplicates

import com.dashlane.auditduplicates.grouping.AuthentifiantForGrouping
import com.dashlane.auditduplicates.grouping.CalculateDuplicatesGroups
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.util.userfeatures.UserFeaturesChecker



class AuditDuplicates(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val credentialDataQuery: CredentialDataQuery,
    private val calculateDuplicatesGroups: CalculateDuplicatesGroups,
    private val auditDuplicatesLogger: AuditDuplicatesLogger,
    private val auditDuplicatesRepository: AuditDuplicatesRepository
) {
    fun startAudit() {
        if (!userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.AUDIT_DUPLICATES)) {
            return
        }

        if (auditDuplicatesRepository.isAuditProcessed()) {
            return
        }

        val accounts = credentialDataQuery.queryAll()
            .map { AuthentifiantForGrouping(it) }
            .takeIf { it.isNotEmpty() }
            ?: return

        runCatching {
            val calculatedGroups = calculateDuplicatesGroups.calculateGroups(accounts)
            auditDuplicatesLogger.logAuditResults(calculatedGroups)
        }
        auditDuplicatesRepository.setAuditAsProcessed()
    }
}
