package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class SharingFlowType(override val code: String) : TrackingLog.Enum {
    COLLECTION_SHARING(
        code = "collection_sharing",
    ),
    ITEM_SHARING(
        code = "item_sharing",
    ),
}