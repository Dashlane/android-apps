package com.dashlane.securefile

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode122
import com.dashlane.useractivity.log.usage.UsageLogCode123
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class SecureFileLogger @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {
    fun logChoose() =
        log(UsageLogCode122.Type.UPLOAD, UsageLogCode122.Action.CLICK)

    fun logUploadStart(anonymousId: String) =
        log(UsageLogCode122.Type.UPLOAD, UsageLogCode122.Action.START, anonymousId)

    fun logUploadError(step: String, anonymousId: String) =
        log(UsageLogCode122.Type.UPLOAD, UsageLogCode122.Action.ERROR, subAction = step, anonymousId = anonymousId)

    fun logUploadSuccess(anonymousId: String) =
        log(UsageLogCode122.Type.UPLOAD, UsageLogCode122.Action.SUCCESS, anonymousId)

    fun logDownloadStart(anonymousId: String?) =
        log(UsageLogCode122.Type.DOWNLOAD, UsageLogCode122.Action.START, anonymousId)

    fun logDownloadError(anonymousId: String?, step: String) =
        log(UsageLogCode122.Type.DOWNLOAD, UsageLogCode122.Action.ERROR, subAction = step, anonymousId = anonymousId)

    fun logDownloadSuccess(anonymousId: String?) =
        log(UsageLogCode122.Type.DOWNLOAD, UsageLogCode122.Action.SUCCESS, anonymousId)

    fun logOpen(anonymousId: String?) =
        log(UsageLogCode122.Type.VIEW, UsageLogCode122.Action.START, anonymousId)

    fun logDelete(anonymousId: String?) =
        log(UsageLogCode122.Type.DELETE, UsageLogCode122.Action.CLICK, anonymousId)

    fun logDeleteError(anonymousId: String?) =
        log(UsageLogCode122.Type.DELETE, UsageLogCode122.Action.ERROR, anonymousId)

    fun logFileDetails(
        action: UsageLogCode123.Action,
        fileExtension: String,
        localSize: Long,
        uploadSize: Long,
        itemId: String
    ) {
        send(
            UsageLogCode123(
                action = action,
                fileExtension = fileExtension,
                localSize = localSize,
                uploadSize = uploadSize,
                itemId = itemId
            )
        )
    }

    private fun log(
        type: UsageLogCode122.Type,
        action: UsageLogCode122.Action,
        anonymousId: String? = null,
        subAction: String? = null
    ) = send(
        UsageLogCode122(
            type = type,
            action = action,
            itemId = anonymousId,
            actionSub = subAction
        )
    )

    private fun send(log: UsageLog) {
        val session = sessionManager.session ?: return
        bySessionUsageLogRepository[session]?.enqueue(log)
    }
}