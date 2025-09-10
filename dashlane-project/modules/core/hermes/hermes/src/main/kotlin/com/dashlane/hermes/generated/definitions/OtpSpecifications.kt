package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
data class OtpSpecifications(
    val durationOtpValidity: Int? = null,
    val encryptionAlgorithm: EncryptionAlgorithm? = null,
    val otpType: OtpType,
    val otpIncrementCount: Int? = null,
    val otpCodeSize: Int? = null,
) : TrackingLog.Object {
    override fun collect(collector: TrackingLog.Collector) {
        collector["duration_otp_validity"] = durationOtpValidity
        collector["encryption_algorithm"] = encryptionAlgorithm
        collector["otp_type"] = otpType
        collector["otp_increment_count"] = otpIncrementCount
        collector["otp_code_size"] = otpCodeSize
    }
}