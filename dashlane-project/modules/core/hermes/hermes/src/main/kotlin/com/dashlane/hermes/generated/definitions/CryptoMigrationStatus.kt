package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CryptoMigrationStatus(override val code: String) : TrackingLog.Enum {
    ERROR_DOWNLOAD(
        code = "error_download",
    ),
    ERROR_REENCRYPTION(
        code = "error_reencryption",
    ),
    ERROR_UPDATE_LOCAL_DATA(
        code = "error_update_local_data",
    ),
    ERROR_UPLOAD(
        code = "error_upload",
    ),
    SUCCESS(
        code = "success",
    ),
}