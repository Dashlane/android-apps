package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FlowStep
import com.dashlane.hermes.generated.definitions.FlowType
import com.dashlane.hermes.generated.definitions.SettingLevel
import javax.annotation.processing.Generated

@Generated
data class ChangeProtectWithMasterPasswordSetting(
    val flowType: FlowType,
    val flowStep: FlowStep,
    val settingLevel: SettingLevel,
) : TrackingLog {
    override val name
        get() = "change_protect_with_master_password_setting"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["flow_type"] = flowType
        collector["flow_step"] = flowStep
        collector["setting_level"] = settingLevel
    }
}