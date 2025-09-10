package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Mode(override val code: String) : TrackingLog.Enum {
    BIOMETRIC(
        code = "biometric",
    ),
    DEVICE_TRANSFER(
        code = "device_transfer",
    ),
    MASTER_PASSWORD(
        code = "master_password",
    ),
    NOT_SELECTED(
        code = "not_selected",
    ),
    PASSKEY(
        code = "passkey",
    ),
    PIN(
        code = "pin",
    ),
    SSO(
        code = "sso",
    ),
}