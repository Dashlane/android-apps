package com.dashlane.auditduplicates



interface AuditDuplicatesRepository {
    fun isAuditProcessed(): Boolean
    fun setAuditAsProcessed()
}
