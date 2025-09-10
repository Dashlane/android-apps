package com.dashlane.premium.current.model

import androidx.annotation.StringRes
import com.dashlane.ui.model.TextResource

internal data class CurrentPlan(
    val title: TextResource,
    val benefits: List<Benefit>,
    val suggestion: Suggestion?,
    val primaryAction: Action,
    val secondaryAction: Action?,
) {
    data class Suggestion(
        val title: TextResource?,
        val text: TextResource,
    )

    data class Benefit(
        val textResource: TextResource,
        val moreInfoType: MoreInfoType?,
    ) {
        enum class MoreInfoType {
            DARK_WEB_MONITORING,
        }
    }

    data class Action(
        val type: Type,
        @StringRes val label: Int,
    ) {
        enum class Type {
            ALL_PLANS,
            PREMIUM,
            FAMILY,
            CLOSE,
        }
    }
}