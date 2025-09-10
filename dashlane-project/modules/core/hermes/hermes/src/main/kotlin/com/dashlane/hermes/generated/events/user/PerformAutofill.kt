package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.AutofillOrigin
import com.dashlane.hermes.generated.definitions.CredentialSecurityStatus
import com.dashlane.hermes.generated.definitions.FieldsFilled
import com.dashlane.hermes.generated.definitions.FormType
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemSource
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.hermes.generated.definitions.Space
import javax.annotation.processing.Generated

@Generated
data class PerformAutofill(
    val autofillOrigin: AutofillOrigin,
    val isManual: Boolean,
    val fieldsFilled: FieldsFilled? = null,
    val credentialFilledItemId: ItemId? = null,
    val space: Space? = null,
    val isAutologin: Boolean,
    val itemSource: ItemSource? = null,
    val mobileBrowserName: String? = null,
    val passwordFilledHealth: CredentialSecurityStatus? = null,
    val formTypeList: List<FormType>? = null,
    val autofillMechanism: AutofillMechanism,
    val matchType: MatchType,
) : TrackingLog {
    override val name
        get() = "perform_autofill"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["autofill_origin"] = autofillOrigin
        collector["is_manual"] = isManual
        collector["fields_filled"] = fieldsFilled
        collector["credential_filled_item_id"] = credentialFilledItemId
        collector["space"] = space
        collector["is_autologin"] = isAutologin
        collector["item_source"] = itemSource
        collector["mobile_browser_name"] = mobileBrowserName
        collector["password_filled_health"] = passwordFilledHealth
        collector["form_type_list"] = formTypeList
        collector["autofill_mechanism"] = autofillMechanism
        collector["match_type"] = matchType
    }
}