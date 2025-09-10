package com.dashlane.password.generator

open class PasswordGeneratorException(
    message: String?,
    cause: Throwable? = null,
) : Exception(message, cause)