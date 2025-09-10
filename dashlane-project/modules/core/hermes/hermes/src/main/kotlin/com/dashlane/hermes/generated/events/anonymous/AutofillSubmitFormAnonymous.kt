package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.FieldsFilledByCount
import com.dashlane.hermes.generated.definitions.FormType
import java.time.ZonedDateTime
import javax.annotation.processing.Generated

@Generated
data class AutofillSubmitFormAnonymous(
    val hasPasswordField: Boolean,
    val totalFormFieldsCount: Int,
    val domain: Domain,
    val formLoadedAtDateTime: ZonedDateTime,
    val fieldsDetectedByAnalysisEngineCount: Int,
    val formType: FormType,
    val formSubmittedAtDateTime: ZonedDateTime,
    val fieldsFilledBy: FieldsFilledByCount,
    val hasModifiedInitiallyAutofilledValue: Boolean,
) : TrackingLog {
    override val name
        get() = "autofill_submit_form"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["has_password_field"] = hasPasswordField
        collector["total_form_fields_count"] = totalFormFieldsCount
        collector["domain"] = domain
        collector["form_loaded_at_date_time"] = formLoadedAtDateTime
        collector["fields_detected_by_analysis_engine_count"] = fieldsDetectedByAnalysisEngineCount
        collector["form_type"] = formType
        collector["form_submitted_at_date_time"] = formSubmittedAtDateTime
        collector["fields_filled_by"] = fieldsFilledBy
        collector["has_modified_initially_autofilled_value"] = hasModifiedInitiallyAutofilledValue
    }
}