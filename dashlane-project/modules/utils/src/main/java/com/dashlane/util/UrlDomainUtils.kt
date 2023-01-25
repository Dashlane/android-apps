package com.dashlane.util

import com.dashlane.url.UrlDomain



fun UrlDomain?.valueWithoutWww(): String? = this?.value?.removePrefix("www.")
