package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.PackageSource
import com.dashlane.hermes.generated.definitions.RemoteFileUpdateError
import javax.annotation.processing.Generated

@Generated
data class ReceiveRemoteFileUpdate(
    val flowStep: FlowStep,
    val remoteFileUpdateError: RemoteFileUpdateError? = null,
    val packageSource: PackageSource? = null,
) : TrackingLog {
    override val name
        get() = "receive_remote_file_update"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["flow_step"] = flowStep
        collector["remote_file_update_error"] = remoteFileUpdateError
        collector["package_source"] = packageSource
    }
}