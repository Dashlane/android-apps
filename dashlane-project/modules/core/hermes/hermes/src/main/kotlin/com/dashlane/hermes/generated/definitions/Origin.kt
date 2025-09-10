package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Origin(override val code: String) : TrackingLog.Enum {
    COLLECTION_DETAIL_VIEW(
        code = "collection_detail_view",
    ),
    COLLECTION_LEFT_HAND_SIDE_MENU_QUICK_ACTIONS_DROPDOWN(
        code = "collection_left_hand_side_menu_quick_actions_dropdown",
    ),
    COLLECTION_LIST_VIEW(
        code = "collection_list_view",
    ),
    COLLECTION_LIST_VIEW_QUICK_ACTIONS_DROPDOWN(
        code = "collection_list_view_quick_actions_dropdown",
    ),
    ITEM_DETAIL_VIEW(
        code = "item_detail_view",
    ),
    ITEM_LIST_VIEW(
        code = "item_list_view",
    ),
    ITEM_LIST_VIEW_QUICK_ACTIONS_DROPDOWN(
        code = "item_list_view_quick_actions_dropdown",
    ),
    SEARCH_QUICK_ACTIONS_DROPDOWN(
        code = "search_quick_actions_dropdown",
    ),
}