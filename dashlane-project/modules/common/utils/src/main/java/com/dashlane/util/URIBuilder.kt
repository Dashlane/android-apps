package com.dashlane.util

import java.net.URI

class URIBuilder(
    val scheme: String = "https",
    val authority: String
) {
    var fragment: String? = null
    private val paths = mutableListOf<String>()
    private val queries = mutableListOf<Query>()

    fun appendPath(path: String) {
        paths.add(path)
    }

    fun appendQueryParameter(key: String, value: String) {
        queries.add(Query(key = key, value = value))
    }

    fun build(): URI {
        return URI.create(
            buildString {
                append(scheme)
                append("://")
                append(authority.removeSuffix("/"))
                append(paths.joinToString(prefix = "/", separator = "/"))
                append(queries.joinToString(prefix = "?", separator = "&") { "${it.key}=${it.value}" })
                fragment?.let { append("#$it") }
            }
        )
    }

    private data class Query(val key: String, val value: String)
}