package com.dashlane.navigation

import android.net.Uri

class NavigationUriBuilder private constructor(private var builder: Uri.Builder) {

    constructor() : this(Uri.Builder())

    init {
        
        builder.scheme(NavigationHelper.Destination.SCHEME).authority("")
    }

    fun host(host: String) = apply {
        
        if (host.isNotBlank()) builder.appendPath(host)
    }

    fun origin(origin: String) = appendQueryParameter("origin", origin)

    fun appendQueryParameter(key: String, value: String) = apply { builder.appendQueryParameter(key, value) }

    fun appendPath(pathPart: String) = apply { builder.appendPath(pathPart) }

    fun build() = builder.build()

    companion object {
        @JvmStatic
        fun with(uri: Uri) = NavigationUriBuilder(uri.buildUpon())
    }
}