package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ImportDataDropAction(override val code: String) : TrackingLog.Enum {
    CANCEL_PROCESS(
        code = "cancel_process",
    ),
    SHUT_DOWN_BROWSER_TAB(
        code = "shut_down_browser_tab",
    ),
    SWITCHED_WEBAPP_SECTION(
        code = "switched_webapp_section",
    ),
}