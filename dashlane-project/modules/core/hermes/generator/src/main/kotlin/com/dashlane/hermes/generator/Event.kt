package com.dashlane.hermes.generator

import java.net.URI

data class Event(
    val id: URI,
    val description: String,
    val type: String,
    val properties: Map<String, Property>,
    val category: Category,
    val priority: Boolean,
    val deprecated: Boolean,
) {
    data class Property(
        val id: URI,
        val required: Boolean,
    )

    enum class Category(val folderName: String) {
        USER("user"),
        ANONYMOUS("anonymous"),
    }
}