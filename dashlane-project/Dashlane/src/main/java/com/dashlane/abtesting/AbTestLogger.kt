package com.dashlane.abtesting

import com.dashlane.session.Username

interface AbTestLogger {
    

    fun logVariant(status: AbTestStatus, isPriorityLog: Boolean = false): Boolean

    

    fun logVariants(list: List<AbTestStatus>): Boolean

    

    fun logVariantOnce(
        username: Username,
        abTestStatus: AbTestStatus,
        isPriorityLog: Boolean = false
    ): Boolean
}