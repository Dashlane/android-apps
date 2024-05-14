package com.dashlane.sync.cryptochanger

import com.dashlane.server.api.exceptions.DashlaneApiException

open class SyncCryptoChangerException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

open class SyncCryptoChangerDownloadException(
    message: String? = null,
    override val cause: DashlaneApiException
) : Exception(message, cause)

open class SyncCryptoChangerUploadException(
    message: String? = null,
    override val cause: DashlaneApiException
) : Exception(message, cause)

open class SyncCryptoChangerCryptographyException(
    message: String? = null,
    cause: Throwable? = null
) : SyncCryptoChangerException(message, cause)