package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class RightClickMenuFlowStep(override val code: String) : TrackingLog.Enum {
    CLICK_DASHLANE(
        code = "click_dashlane",
    ),
    DISPLAY_MENU(
        code = "display_menu",
    ),
    SELECT_VAULT_ITEM(
        code = "select_vault_item",
    ),
}