package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CorrectionType
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class AutofillCorrect(
    val fieldCorrected: ItemType,
    val initialFieldClassificationList: List<ItemType>? = null,
    val correctionType: CorrectionType,
    val newFieldClassification: ItemType? = null,
) : TrackingLog {
    override val name
        get() = "autofill_correct"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["field_corrected"] = fieldCorrected
        collector["initial_field_classification_list"] = initialFieldClassificationList
        collector["correction_type"] = correctionType
        collector["new_field_classification"] = newFieldClassification
    }
}