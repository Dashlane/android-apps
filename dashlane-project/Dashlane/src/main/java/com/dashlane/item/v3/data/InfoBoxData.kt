package com.dashlane.item.v3.data

import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.model.TextResource

data class InfoBoxData(val title: TextResource, val description: TextResource, val mood: Mood, val button: Button? = null) {
    data class Button(val text: TextResource, val action: Action) {
        enum class Action {
            LAUNCH_GUIDED_CHANGE
        }
    }
}