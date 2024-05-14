package com.dashlane.authentication.sso.utils

import android.net.Uri

internal fun String.toIdpUrl(login: String): Uri = Uri.parse(this)
    .buildUpon()
    .appendQueryParameter("username", login)
    .appendQueryParameter("redirect", "mobile")
    
    .appendQueryParameter("frag", "true")
    .build()
