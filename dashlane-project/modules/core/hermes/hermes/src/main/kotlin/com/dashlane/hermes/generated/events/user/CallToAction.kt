package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CallToAction
import com.dashlane.hermes.generated.definitions.ToastName
import javax.annotation.processing.Generated

@Generated
data class CallToAction(
    val toastName: ToastName? = null,
    val hasChosenNoAction: Boolean,
    val callToActionList: List<CallToAction>? = null,
    val chosenAction: CallToAction? = null,
) : TrackingLog {
    override val name
        get() = "call_to_action"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["toast_name"] = toastName
        collector["has_chosen_no_action"] = hasChosenNoAction
        collector["call_to_action_list"] = callToActionList
        collector["chosen_action"] = chosenAction
    }
}