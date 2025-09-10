package com.dashlane.item.v3.data

import com.dashlane.design.theme.color.Mood
import com.dashlane.ui.model.TextResource

data class InfoBoxData(
    val title: TextResource,
    val description: TextResource? = null,
    val mood: Mood,
    val button: Button? = null,
    val infoboxStyle: InfoboxStyle = InfoboxStyle.LARGE,
) {
    data class Button(val text: TextResource, val action: Action) {
        enum class Action {
            LAUNCH_GUIDED_CHANGE,
            OPEN_NETWORK_SETTINGS,
            OPEN_LOCATION_SETTINGS,
            ENABLE_LOCATION_PERMISSION,
        }
    }
}

enum class InfoboxStyle {
    LARGE,
    MEDIUM,
}