package com.dashlane.storage.userdata.dao

data class ItemContentDB(
    val id: String,
    val timestamp: Long,
    val extraData: String,
    val itemKeyBase64: String
)
