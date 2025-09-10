package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.CorrectionType
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class AutofillCorrectAnonymous(
    val fieldCorrected: ItemType,
    val initialFieldClassificationList: List<ItemType>? = null,
    val domain: Domain,
    val correctionType: CorrectionType,
    val isNativeApp: Boolean,
    val newFieldClassification: ItemType? = null,
) : TrackingLog {
    override val name
        get() = "autofill_correct"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["field_corrected"] = fieldCorrected
        collector["initial_field_classification_list"] = initialFieldClassificationList
        collector["domain"] = domain
        collector["correction_type"] = correctionType
        collector["is_native_app"] = isNativeApp
        collector["new_field_classification"] = newFieldClassification
    }
}