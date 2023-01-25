package com.dashlane.ui.screens.sharing

import com.dashlane.core.domain.sharing.SharingPermission
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.fragments.SharingUlHelper.createUsageLogCode80
import com.dashlane.useractivity.log.usage.UsageLogCode80
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class SharingLogger @Inject constructor(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager,
) {
    private val usageLogRepository: UsageLogRepository?
        get() = bySessionUsageLogRepository[sessionManager.session]

    fun logNewShareStep2Next(
        from: UsageLogCode80.From?,
        accountSize: Int,
        secureNoteSize: Int,
        emailSize: Int,
        permission: SharingPermission
    ) {
        val usageLogCode80 = createUsageLogCode80(
            UsageLogCode80.Type.NEW_SHARE2,
            from,
            UsageLogCode80.Action.NEXT,
            accountSize,
            secureNoteSize,
            0,
            emailSize,
            false,
            permission === SharingPermission.ADMIN
        )
        usageLogRepository?.enqueue(usageLogCode80, false)
    }

    fun logNewShareStep2Back(
        from: UsageLogCode80.From?,
        accountSize: Int,
        secureNoteSize: Int,
        emailSize: Int,
        permission: SharingPermission
    ) {
        val usageLogCode80 = createUsageLogCode80(
            UsageLogCode80.Type.NEW_SHARE2,
            from,
            UsageLogCode80.Action.BACK,
            accountSize,
            secureNoteSize,
            0,
            emailSize,
            false,
            permission === SharingPermission.ADMIN
        )
        usageLogRepository?.enqueue(usageLogCode80, false)
    }
}