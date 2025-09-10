package com.dashlane.database

import com.dashlane.cryptography.CryptographyException

open class DatabaseException(
    message: String? = "General database exception",
    cause: Throwable? = null,
) : Exception(message, cause)

class DatabaseCryptographyException(cryptographyException: CryptographyException) :
    DatabaseException(cause = cryptographyException)

class DatabaseIndexInvalidException(
    message: String? = "Can not read database index",
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseIndexNotFoundException(
    message: String? = "Index doesn't exist",
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseIndexUserNotMatchException(
    message: String? = "Index user not match",
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseIndexInitializationException(
    message: String? = "Can not initialize index file",
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseAlreadyExistException(
    message: String? = "Recreating an existing database",
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseKeyMarkerNotMatchException(
    message: String?,
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseRebuildSummaryException(
    message: String? = "Cannot rebuild summary",
    cause: Throwable? = null,
) : DatabaseException(message, cause)

class DatabaseRecoveryException(
    message: String? = "Cannot perform recovery",
    cause: Throwable? = null,
) : DatabaseException(message, cause)