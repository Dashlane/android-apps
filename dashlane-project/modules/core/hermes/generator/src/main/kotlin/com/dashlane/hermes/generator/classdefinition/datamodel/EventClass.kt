package com.dashlane.hermes.generator.classdefinition.datamodel

import com.dashlane.hermes.generator.Definition
import com.dashlane.hermes.generator.Event
import com.dashlane.hermes.generator.className
import com.dashlane.hermes.generator.snakeCaseToCamelCase
import java.net.URI

data class EventClass(
    val imports: List<String>,
    val documentation: String,
    val className: String,
    val params: List<Param>,
    val eventName: String,
    val eventCategory: String,
    val eventPriority: Boolean,
    val deprecated: Boolean,
) {
    data class Param(
        val name: String,
        val documentation: String,
        val type: String,
        val key: String,
    )
}

fun Event.toClass(definitions: Map<URI, Definition>): EventClass {
    val event =
        EventClass(
            imports =
                (
                    properties.values.mapNotNull {
                        definitions.getValue(it.id).getImport(definitions)
                    } + "com.dashlane.hermes.TrackingLog"
                ).distinct().sorted(),
            documentation = description
                .split("\r\r").joinToString(separator = "\n") { " * $it".trimEnd() },
            className = id.className.addClassSuffix(category),
            params =
                properties.map { (name, it) ->
                    val definitionItem = definitions.getValue(it.id)
                    val definition =
                        if (definitionItem.type is Definition.Type.DirectReference) {
                            definitions.getValue(
                                definitionItem.type.ref,
                            ) 
                        } else {
                            definitionItem
                        }
                    EventClass.Param(
                        name = name.snakeCaseToCamelCase(),
                        documentation = definition.description.trim(),
                        type = definition.getKotlinType(definitions) + if (it.required) "" else "? = null",
                        key = name,
                    )
                },
            eventName = type,
            eventCategory =
                when (category) {
                    Event.Category.USER -> "USER"
                    Event.Category.ANONYMOUS -> "ANONYMOUS"
                },
            eventPriority = this.priority,
            deprecated = this.deprecated,
        )
    return event
}

private fun String.addClassSuffix(category: Event.Category) = this + if (category == Event.Category.ANONYMOUS) "Anonymous" else ""

private fun Definition.getImport(definitions: Map<URI, Definition>): String? =
    when (type) {
        is Definition.Type.Boolean,
        is Definition.Type.Double,
        is Definition.Type.Int,
        is Definition.Type.PageEnum,
        is Definition.Type.String,
        ->
            null
        is Definition.Type.DirectReference -> definitions.getValue(type.ref).getImport(definitions)
        is Definition.Type.Uuid ->
            "java.util.UUID"
        is Definition.Type.Date ->
            "java.time.LocalDate"
        is Definition.Type.DateTime ->
            "java.time.ZonedDateTime"
        is Definition.Type.Sha256 ->
            "com.dashlane.hermes.Sha256Hash"
        is Definition.Type.StringEnum,
        Definition.Type.ItemId,
        is Definition.Type.Object,
        ->
            "com.dashlane.hermes.generated.definitions.${id.className}"
        is Definition.Type.List ->
            definitions.getValue(type.items).getImport(definitions)
    }

private fun Definition.getKotlinType(definitions: Map<URI, Definition>): String =
    when (type) {
        is Definition.Type.Boolean -> "Boolean"
        is Definition.Type.Double -> "Double"
        is Definition.Type.Int -> "Int"
        is Definition.Type.String -> "String"
        is Definition.Type.Uuid -> "UUID"
        is Definition.Type.Date -> "LocalDate"
        is Definition.Type.DateTime -> "ZonedDateTime"
        is Definition.Type.Sha256 -> "Sha256Hash"
        is Definition.Type.StringEnum,
        is Definition.Type.PageEnum,
        is Definition.Type.DirectReference,
        is Definition.Type.ItemId,
        is Definition.Type.Object,
        ->
            id.className
        is Definition.Type.List ->
            "List<${definitions.getValue(type.items).getKotlinType(definitions)}>"
    }