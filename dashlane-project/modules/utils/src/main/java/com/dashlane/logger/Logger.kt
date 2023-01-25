package com.dashlane.logger

private const val TAG_EXCEPTION = "EXCEPTION"
private const val MESSAGE_EXCEPTION = "Exception thrown."



fun Log.v(e: Throwable) {
    v(TAG_EXCEPTION, MESSAGE_EXCEPTION, e)
}
