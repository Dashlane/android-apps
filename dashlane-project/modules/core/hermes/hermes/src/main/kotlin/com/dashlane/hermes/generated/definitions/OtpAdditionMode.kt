package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class OtpAdditionMode(override val code: String) : TrackingLog.Enum {
    QR_CODE(
        code = "qr_code",
    ),
    TEXT_CODE(
        code = "text_code",
    ),
}