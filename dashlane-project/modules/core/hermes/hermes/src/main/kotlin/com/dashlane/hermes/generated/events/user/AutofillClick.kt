package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AutofillButton
import javax.annotation.processing.Generated

@Generated
data class AutofillClick(
    val autofillButton: AutofillButton,
) : TrackingLog {
    override val name
        get() = "autofill_click"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["autofill_button"] = autofillButton
    }
}