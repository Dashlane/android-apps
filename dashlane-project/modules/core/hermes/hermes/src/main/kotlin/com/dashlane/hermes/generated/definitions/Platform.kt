package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Platform(override val code: String) : TrackingLog.Enum {
    ANDROID(
        code = "android",
    ),
    AUTHENTICATOR_ANDROID(
        code = "authenticator_android",
    ),
    AUTHENTICATOR_IOS(
        code = "authenticator_ios",
    ),
    CATALYST(
        code = "catalyst",
    ),
    IOS(
        code = "ios",
    ),
    SAEX(
        code = "saex",
    ),
    SAFARI(
        code = "safari",
    ),
    TAC(
        code = "tac",
    ),
    WEARABLE_ANDROID(
        code = "wearable_android",
    ),
    WEARABLE_IOS(
        code = "wearable_ios",
    ),
    WEB(
        code = "web",
    ),
}