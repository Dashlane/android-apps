package com.dashlane.exception

class NotLoggedInException @JvmOverloads constructor(
    message: String? = null,
    exception: Exception? = null
) : Exception(message, exception)
