package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.DataSource
import com.dashlane.hermes.generated.definitions.FlowStep
import javax.annotation.processing.Generated

@Generated
data class DownloadUserData(
    val flowStep: FlowStep,
    val fileType: BackupFileType,
    val dataSource: DataSource,
) : TrackingLog {
    override val name
        get() = "download_user_data"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["flow_step"] = flowStep
        collector["file_type"] = fileType
        collector["data_source"] = dataSource
    }
}