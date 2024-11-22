package com.dashlane.exception

data class NotLoggedInException(
    override val message: String? = null,
    val exception: Exception? = null
) : Exception(message, exception)
