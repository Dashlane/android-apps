package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class GeneratePassword(
    val hasSimilar: Boolean,
    val hasLetters: Boolean,
    val length: Int,
    val hasSymbols: Boolean,
    val hasDigits: Boolean,
) : TrackingLog {
    override val name
        get() = "generate_password"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["has_similar"] = hasSimilar
        collector["has_letters"] = hasLetters
        collector["length"] = length
        collector["has_symbols"] = hasSymbols
        collector["has_digits"] = hasDigits
    }
}