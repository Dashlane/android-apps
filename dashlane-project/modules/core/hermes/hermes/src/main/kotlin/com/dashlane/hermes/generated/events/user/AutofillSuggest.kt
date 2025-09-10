package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AutofillMessageType
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.PhishingRisk
import com.dashlane.hermes.generated.definitions.WebcardSaveOptions
import javax.annotation.processing.Generated

@Generated
data class AutofillSuggest(
    val phishingRisk: PhishingRisk? = null,
    val webcardItemTotalCount: Int? = null,
    val autofillMessageTypeList: List<AutofillMessageType>? = null,
    val msToWebcard: Int? = null,
    val isLoginPrefilled: Boolean? = null,
    val vaultTypeList: List<ItemType>? = null,
    val isPasswordPrefilled: Boolean? = null,
    val webcardSaveOptions: List<WebcardSaveOptions>? = null,
    val isNativeApp: Boolean,
    val isSuggestLastUnsaved: Boolean? = null,
    val isRestoredWebcard: Boolean? = null,
) : TrackingLog {
    override val name
        get() = "autofill_suggest"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["phishing_risk"] = phishingRisk
        collector["webcard_item_total_count"] = webcardItemTotalCount
        collector["autofill_message_type_list"] = autofillMessageTypeList
        collector["ms_to_webcard"] = msToWebcard
        collector["is_login_prefilled"] = isLoginPrefilled
        collector["vault_type_list"] = vaultTypeList
        collector["is_password_prefilled"] = isPasswordPrefilled
        collector["webcard_save_options"] = webcardSaveOptions
        collector["is_native_app"] = isNativeApp
        collector["is_suggest_last_unsaved"] = isSuggestLastUnsaved
        collector["is_restored_webcard"] = isRestoredWebcard
    }
}