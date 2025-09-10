package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ScimSetupStep(override val code: String) : TrackingLog.Enum {
    ACTIVATE_DIRECTORY_SYNC(
        code = "activate_directory_sync",
    ),
    ACTIVATE_GROUP_SYNC(
        code = "activate_group_sync",
    ),
    CLICK_SET_UP(
        code = "click_set_up",
    ),
    COPY_API_TOKEN(
        code = "copy_api_token",
    ),
    COPY_ENDPOINT_LINK(
        code = "copy_endpoint_link",
    ),
    DEACTIVATE_DIRECTORY_SYNC(
        code = "deactivate_directory_sync",
    ),
    GENERATE_SCIM_TOKEN(
        code = "generate_scim_token",
    ),
    RE_GENERATE_TOKEN(
        code = "re_generate_token",
    ),
}