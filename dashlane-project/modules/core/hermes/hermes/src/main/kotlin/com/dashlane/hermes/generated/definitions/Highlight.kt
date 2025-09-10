package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Highlight(override val code: String) : TrackingLog.Enum {
    MOST_RECENT(
        code = "most_recent",
    ),
    NONE(
        code = "none",
    ),
    SEARCH_RECENT(
        code = "search_recent",
    ),
    SEARCH_RESULT(
        code = "search_result",
    ),
    SUGGESTED(
        code = "suggested",
    ),
}