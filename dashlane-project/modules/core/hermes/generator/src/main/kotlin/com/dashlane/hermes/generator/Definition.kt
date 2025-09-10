package com.dashlane.hermes.generator

import java.net.URI

data class Definition(
    val id: URI,
    val description: String,
    val type: Type,
    val deprecated: Boolean,
) {
    sealed class Type {
        object Boolean : Type()

        object Double : Type()

        object Int : Type()

        object String : Type()

        object Uuid : Type()

        object Date : Type()

        object DateTime : Type()

        object Sha256 : Type()

        object ItemId : Type()

        data class StringEnum(val values: kotlin.collections.List<kotlin.String>) : Type()

        data class List(val items: URI) : Type()

        data class Object(
            val properties: Map<kotlin.String, Property>,
        ) : Type() {
            data class Property(
                val id: URI,
                val required: kotlin.Boolean,
            )
        }

        data class PageEnum(
            val pagesRef: URI,
            val priorityPagesRef: URI,
        ) : Type()

        data class DirectReference(
            val ref: URI,
        ) : Type()
    }
}