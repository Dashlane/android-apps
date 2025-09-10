package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CollectionSelectOrigin(override val code: String) : TrackingLog.Enum {
    COLLECTION_LIST(
        code = "collection_list",
    ),
    ITEM_LIST(
        code = "item_list",
    ),
    LEFT_HAND_SIDE_MENU(
        code = "left_hand_side_menu",
    ),
    SEARCH_RESULTS(
        code = "search_results",
    ),
}