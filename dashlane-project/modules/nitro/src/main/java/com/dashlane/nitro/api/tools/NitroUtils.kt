package com.dashlane.nitro.api.tools

import com.dashlane.url.toHttpUrl
import com.dashlane.url.toUrlDomain

internal fun String.toHostUrl() = toUrlDomain().toHttpUrl().toString().dropLast(1)
