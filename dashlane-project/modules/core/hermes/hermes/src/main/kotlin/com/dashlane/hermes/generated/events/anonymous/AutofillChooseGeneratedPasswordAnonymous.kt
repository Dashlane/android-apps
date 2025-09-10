package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Domain
import javax.annotation.processing.Generated

@Generated
data class AutofillChooseGeneratedPasswordAnonymous(
    val hasSimilar: Boolean,
    val hasLetters: Boolean,
    val domain: Domain,
    val length: Int,
    val hasSymbols: Boolean,
    val isNativeApp: Boolean? = null,
    val hasDigits: Boolean,
    val hasPwgDefaultSettings: Boolean? = null,
) : TrackingLog {
    override val name
        get() = "autofill_choose_generated_password"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["has_similar"] = hasSimilar
        collector["has_letters"] = hasLetters
        collector["domain"] = domain
        collector["length"] = length
        collector["has_symbols"] = hasSymbols
        collector["is_native_app"] = isNativeApp
        collector["has_digits"] = hasDigits
        collector["has_pwg_default_settings"] = hasPwgDefaultSettings
    }
}