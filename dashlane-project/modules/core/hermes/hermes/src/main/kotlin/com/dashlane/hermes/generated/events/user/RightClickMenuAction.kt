package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.RightClickMenuFlowStep
import javax.annotation.processing.Generated

@Generated
data class RightClickMenuAction(
    val isFieldDetectedByAnalysisEngine: Boolean,
    val rightClickMenuFlowStep: RightClickMenuFlowStep,
    val fieldFilled: ItemType? = null,
    val fieldInitialClassificationList: List<ItemType>? = null,
) : TrackingLog {
    override val name
        get() = "right_click_menu_action"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_field_detected_by_analysis_engine"] = isFieldDetectedByAnalysisEngine
        collector["right_click_menu_flow_step"] = rightClickMenuFlowStep
        collector["field_filled"] = fieldFilled
        collector["field_initial_classification_list"] = fieldInitialClassificationList
    }
}