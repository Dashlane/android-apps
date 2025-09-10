package com.dashlane.hermes.generator.classdefinition.datamodel

import com.dashlane.hermes.generator.Definition
import com.dashlane.hermes.generator.className
import com.dashlane.hermes.generator.snakeCaseToCamelCase
import java.net.URI

data class ObjectClass(
    val imports: List<String>,
    val documentation: String,
    val className: String,
    val params: List<Param>,
    val deprecated: Boolean,
) {
    data class Param(
        val name: String,
        val documentation: String,
        val type: String,
        val key: String,
    )
}

fun Definition.Type.Object.toClass(
    id: URI,
    definitions: Map<URI, Definition>,
    documentation: String,
    deprecated: Boolean,
) = ObjectClass(
    imports =
        (
            properties.values.mapNotNull {
                definitions.getValue(it.id).getImport(definitions)
            } + "com.dashlane.hermes.TrackingLog"
        ).distinct().sorted(),
    documentation = documentation,
    className = id.className,
    params =
        properties.map { (name, it) ->
            val definition = definitions.getValue(it.id)
            ObjectClass.Param(
                name = name.snakeCaseToCamelCase(),
                documentation = definition.description.trimEnd(),
                type = definition.getKotlinType(definitions) + if (it.required) "" else "? = null",
                key = name,
            )
        },
    deprecated = deprecated,
)

private fun Definition.getImport(definitions: Map<URI, Definition>): String? =
    when (type) {
        is Definition.Type.Boolean,
        is Definition.Type.Double,
        is Definition.Type.Int,
        is Definition.Type.String,
        is Definition.Type.StringEnum,
        is Definition.Type.PageEnum,
        is Definition.Type.DirectReference,
        is Definition.Type.Object,
        ->
            null
        is Definition.Type.ItemId,
        is Definition.Type.Uuid,
        ->
            "java.util.UUID"
        is Definition.Type.Date ->
            "java.time.LocalDate"
        is Definition.Type.DateTime ->
            "java.time.ZonedDateTime"
        is Definition.Type.Sha256 ->
            "com.dashlane.hermes.Sha256Hash"
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