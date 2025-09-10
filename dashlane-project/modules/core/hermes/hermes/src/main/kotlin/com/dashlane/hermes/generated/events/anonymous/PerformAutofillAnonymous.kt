package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.AutofillOrigin
import com.dashlane.hermes.generated.definitions.CredentialSecurityStatus
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.FieldsFilled
import com.dashlane.hermes.generated.definitions.FormType
import com.dashlane.hermes.generated.definitions.MatchType
import javax.annotation.processing.Generated

@Generated
data class PerformAutofillAnonymous(
    val isAutologin: Boolean,
    val mobileBrowserName: String? = null,
    val passwordFilledHealth: CredentialSecurityStatus? = null,
    val formTypeList: List<FormType>? = null,
    val autofillOrigin: AutofillOrigin,
    val domain: Domain,
    val autofillMechanism: AutofillMechanism,
    val isManual: Boolean,
    val matchType: MatchType,
    val fieldsFilled: FieldsFilled? = null,
    val isNativeApp: Boolean,
) : TrackingLog {
    override val name
        get() = "perform_autofill"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_autologin"] = isAutologin
        collector["mobile_browser_name"] = mobileBrowserName
        collector["password_filled_health"] = passwordFilledHealth
        collector["form_type_list"] = formTypeList
        collector["autofill_origin"] = autofillOrigin
        collector["domain"] = domain
        collector["autofill_mechanism"] = autofillMechanism
        collector["is_manual"] = isManual
        collector["match_type"] = matchType
        collector["fields_filled"] = fieldsFilled
        collector["is_native_app"] = isNativeApp
    }
}