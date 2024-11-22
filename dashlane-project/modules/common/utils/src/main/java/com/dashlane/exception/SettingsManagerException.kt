package com.dashlane.exception

class SettingsManagerException @JvmOverloads constructor(message: String? = null, exception: Exception? = null) :
    Exception(message, exception) {
    constructor(exception: Exception) : this(message = null, exception = exception)
}
