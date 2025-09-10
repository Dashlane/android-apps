package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ErrorName(override val code: String) : TrackingLog.Enum {
    AUTHENTICATION(
        code = "authentication",
    ),
    DATA_PROCESSING(
        code = "data_processing",
    ),
    DATABASE(
        code = "database",
    ),
    DOWNLOAD(
        code = "download",
    ),
    HTTP_IO(
        code = "http_io",
    ),
    HTTP_STATUS(
        code = "http_status",
    ),
    ITEM_GROUP_INVALID_GROUP_ACCEPT_SIGNATURE(
        code = "item_group_invalid_group_accept_signature",
    ),
    ITEM_GROUP_INVALID_GROUP_PROPOSE_SIGNATURE(
        code = "item_group_invalid_group_propose_signature",
    ),
    ITEM_GROUP_INVALID_KEY(
        code = "item_group_invalid_key",
    ),
    ITEM_GROUP_INVALID_USER_ACCEPT_SIGNATURE(
        code = "item_group_invalid_user_accept_signature",
    ),
    ITEM_GROUP_INVALID_USER_PROPOSE_SIGNATURE(
        code = "item_group_invalid_user_propose_signature",
    ),
    ITEM_GROUP_NO_ACCESS(
        code = "item_group_no_access",
    ),
    MEMORY(
        code = "memory",
    ),
    OTHER(
        code = "other",
    ),
    RESPONSE_CONTENT(
        code = "response_content",
    ),
    UPLOAD(
        code = "upload",
    ),
}