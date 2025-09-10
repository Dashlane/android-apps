package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class MeasureName(override val code: String) : TrackingLog.Enum {
    SYSTEM_CPU(
        code = "system_cpu",
    ),
    SYSTEM_MEMORY(
        code = "system_memory",
    ),
    TIME_TO_APP_READY(
        code = "time_to_app_ready",
    ),
    TIME_TO_D(
        code = "time_to_d",
    ),
    TIME_TO_DROPDOWN_WEBCARD(
        code = "time_to_dropdown_webcard",
    ),
    TIME_TO_INIT_CARBON(
        code = "time_to_init_carbon",
    ),
    TIME_TO_INIT_PERSISTENT(
        code = "time_to_init_persistent",
    ),
    TIME_TO_INIT_SW(
        code = "time_to_init_sw",
    ),
    TIME_TO_LOAD(
        code = "time_to_load",
    ),
    TIME_TO_LOAD_AUTOFILL(
        code = "time_to_load_autofill",
    ),
    TIME_TO_LOAD_LOCAL(
        code = "time_to_load_local",
    ),
    TIME_TO_LOAD_REMOTE(
        code = "time_to_load_remote",
    ),
    TIME_TO_LOGIN(
        code = "time_to_login",
    ),
    TIME_TO_PREPARE_WEBCARD_DATA(
        code = "time_to_prepare_webcard_data",
    ),
    TIME_TO_UNLOCK(
        code = "time_to_unlock",
    ),
    WEBVITALS_CLS(
        code = "webvitals_cls",
    ),
    WEBVITALS_FID(
        code = "webvitals_fid",
    ),
    WEBVITALS_LCP(
        code = "webvitals_lcp",
    ),
}