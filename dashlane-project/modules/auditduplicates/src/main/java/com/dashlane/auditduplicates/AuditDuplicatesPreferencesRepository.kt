package com.dashlane.auditduplicates

import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager



class AuditDuplicatesPreferencesRepository(
    private val userPreferencesManager: UserPreferencesManager
) : AuditDuplicatesRepository {

    override fun isAuditProcessed(): Boolean {
        return userPreferencesManager.getBoolean(ConstantsPrefs.AUDIT_DUPLICATES_PROCESSED, false)
    }

    override fun setAuditAsProcessed() {
        userPreferencesManager.putBoolean(ConstantsPrefs.AUDIT_DUPLICATES_PROCESSED, true)
    }
}
