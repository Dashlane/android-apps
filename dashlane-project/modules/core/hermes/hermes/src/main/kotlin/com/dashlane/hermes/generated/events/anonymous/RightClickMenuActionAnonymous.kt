package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.RightClickMenuFlowStep
import javax.annotation.processing.Generated

@Generated
data class RightClickMenuActionAnonymous(
    val domain: Domain,
    val isFieldDetectedByAnalysisEngine: Boolean,
    val rightClickMenuFlowStep: RightClickMenuFlowStep,
    val isNativeApp: Boolean,
    val fieldFilled: ItemType? = null,
    val fieldInitialClassificationList: List<ItemType>? = null,
) : TrackingLog {
    override val name
        get() = "right_click_menu_action"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["domain"] = domain
        collector["is_field_detected_by_analysis_engine"] = isFieldDetectedByAnalysisEngine
        collector["right_click_menu_flow_step"] = rightClickMenuFlowStep
        collector["is_native_app"] = isNativeApp
        collector["field_filled"] = fieldFilled
        collector["field_initial_classification_list"] = fieldInitialClassificationList
    }
}