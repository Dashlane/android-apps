package com.dashlane.sync.repositories

data class ServerCredentials(
    val login: String,
    val accessKey: String,
    val secretKey: String,
)