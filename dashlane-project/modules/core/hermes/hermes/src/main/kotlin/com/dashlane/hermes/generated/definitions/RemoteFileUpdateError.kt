package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class RemoteFileUpdateError(override val code: String) : TrackingLog.Enum {
    DECIPHER_ERROR(
        code = "decipher_error",
    ),
    DOWNLOAD_ERROR(
        code = "download_error",
    ),
    LOCAL_STORAGE_ERROR(
        code = "local_storage_error",
    ),
    OTHER_ERROR(
        code = "other_error",
    ),
    SERVER_ERROR(
        code = "server_error",
    ),
}