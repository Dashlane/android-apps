package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AuditLogDownloadError
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class DownloadAuditLogsData(
    val auditLogDownloadError: AuditLogDownloadError? = null,
    val auditLogCount: Int? = null,
    val flowStep: FlowStep,
) : TrackingLog {
    override val name
        get() = "download_audit_logs_data"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["audit_log_download_error"] = auditLogDownloadError
        collector["audit_log_count"] = auditLogCount
        collector["flow_step"] = flowStep
    }
}