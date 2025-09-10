package com.dashlane.xml.model

import org.everit.json.schema.ArraySchema
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.NumberSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.ReferenceSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.SchemaLocation
import org.everit.json.schema.StringSchema
import org.json.JSONObject

data class TransactionType(
    val xmlName: String,
    val transactionName: String,
    val description: String? = null,
    val properties: Map<String, Property>,
) {
    data class Property(
        val type: Type,
        val description: String? = null,
        val shouldDuplicateOnConflict: Boolean = false,
        val shouldCanonizeForConflictComparison: Boolean = false,
        val sensitive: Boolean = false,
        val required: Boolean = false,
        val deprecated: Boolean = false,
        val default: Any? = null,
        val deduplicationSignature: Boolean = false,
    ) {
        sealed class Type {
            object Boolean : Type()

            object String : Type()

            object Integer : Type()

            object Float : Type()

            object Month : Type()

            object Year : Type()

            object LocalDate : Type()

            object Instant : Type()

            object Base64EncodedByteArray : Type()

            object Country : Type()

            data class StringEnum(
                val name: kotlin.String,
                val values: Set<kotlin.String>,
                val isGlobal: kotlin.Boolean,
            ) : Type()

            data class List(val type: Type) : Type()

            data class Object(
                val name: kotlin.String,
                val properties: Map<kotlin.String, Property>,
                val isObject: kotlin.Boolean = false,
            ) : Type()

            data class JsonData(val name: kotlin.String, val data: JSONObject?) : Type()
        }
    }
}

fun Schema.toTransactionType(): TransactionType {
    check(this is CombinedSchema)
    val objectSchema = subschemas.filterIsInstance<ObjectSchema>().single()
    val xmlName = unprocessedProperties["xmlName"] as String
    val transactionName = unprocessedProperties["transactionType"] as String
    val properties = objectSchema.toPropertyMap()
    return TransactionType(xmlName, transactionName, description, properties)
}

fun Schema.toTransactionTypeFromObject(): TransactionType {
    val properties = when (this) {
        is ObjectSchema -> this.toPropertyMap()
        is ArraySchema -> (this.allItemSchema as ObjectSchema).toPropertyMap()
        else -> error("Invalid schema type: $this")
    }
    return TransactionType("xmlName", "transactionName", description, properties)
}

internal fun ObjectSchema.toPropertyMap(): Map<String, TransactionType.Property> =
    propertySchemas.toPropertyMap(requiredProperties.toSet())

internal fun Map<String, Schema>.toPropertyMap(requiredProperties: Set<String>): Map<String, TransactionType.Property> =
    mapValues { (name, schema) -> schema.toProperty(name, requiredProperties.contains(name)) }
        .filterNot { it.value.deprecated }

private fun Schema.toProperty(propertyName: String, required: Boolean): TransactionType.Property {
    val type = toPropertyType(propertyName)
    val shouldDuplicateOnConflict = unprocessedProperties["duplicateConflict"] as Boolean? ?: false
    val shouldCanonizeForConflictComparison = unprocessedProperties["canonize"] as Boolean? ?: false
    val sensitive = unprocessedProperties["sensitive"] as Boolean? ?: false
    val deprecated = unprocessedProperties["deprecated"] as Boolean? ?: false
    val deduplicationSignature =
        unprocessedProperties["deduplicationSignature"] as Boolean? ?: false
    return TransactionType.Property(
        type,
        description,
        shouldDuplicateOnConflict,
        shouldCanonizeForConflictComparison,
        sensitive,
        required,
        deprecated,
        unprocessedProperties["default"],
        deduplicationSignature,
    )
}

@Suppress("LongMethod", "ComplexMethod")
private fun Schema.toPropertyType(propertyName: String): TransactionType.Property.Type =
    when (this) {
        is BooleanSchema -> TransactionType.Property.Type.Boolean
        is StringSchema -> {
            when (val formatName = formatValidator?.formatName()) {
                "unnamed-format" -> TransactionType.Property.Type.String
                "boolean" -> TransactionType.Property.Type.Boolean
                "month" -> TransactionType.Property.Type.Month
                "year" -> TransactionType.Property.Type.Year
                "local-date" -> TransactionType.Property.Type.LocalDate
                "base64" -> TransactionType.Property.Type.Base64EncodedByteArray
                "country" -> TransactionType.Property.Type.Country
                "json" -> {
                    val fileName =
                        (unprocessedProperties["embeddedJson"] as String).split("/").last()
                    val rawJson = getEmbeddedJson(fileName)
                    TransactionType.Property.Type.JsonData(fileName.split(".").first(), rawJson)
                }

                else -> error("Unsupported string format $formatName")
            }
        }
        is NumberSchema -> {
            if (requiresInteger()) {
                when (val numberFormat = unprocessedProperties["numberFormat"] as String?) {
                    null -> TransactionType.Property.Type.Integer
                    "instantSeconds" -> TransactionType.Property.Type.Instant
                    else -> error("Unsupported number format $numberFormat")
                }
            } else {
                TransactionType.Property.Type.Float
            }
        }
        is EnumSchema -> {
            check(possibleValues.all { it is String }) { "Only string enums supported.'\n\t$this" }
            TransactionType.Property.Type.StringEnum(
                location?.getClassName() ?: propertyName,
                possibleValues
                    .map { it as String }
                    .filter { it.isNotEmpty() }
                    .toSet(),
                location != null,
            )
        }
        is ReferenceSchema -> referredSchema.toPropertyType(propertyName)
        is ArraySchema -> TransactionType.Property.Type.List(
            allItemSchema.toPropertyType(propertyName),
        )
        is ObjectSchema -> {
            
            val hasXmlName = unprocessedProperties.containsKey("xmlName")
            val objectName = unprocessedProperties["xmlName"] as String? ?: propertyName
            TransactionType.Property.Type.Object(
                objectName,
                toPropertyMap().toSortedMap(),
                hasXmlName,
            )
        }
        is CombinedSchema -> {
            val enumSchemas = subschemas.filterIsInstance<EnumSchema>()
            val referenceSchemas = subschemas.filterIsInstance<ReferenceSchema>()
            when {
                enumSchemas.isNotEmpty() -> {
                    val enumSchema = enumSchemas.single()
                    when (val enumeratedSchema = (subschemas - enumSchema).single()) {
                        is StringSchema -> TransactionType.Property.Type.StringEnum(
                            enumSchema.location?.getClassName() ?: propertyName,
                            enumSchema.possibleValues
                                .map { it as String }
                                .filter { it.isNotEmpty() }
                                .toSet(),
                            enumSchema.location != null,
                        )

                        else -> error("Unsupported enumerated type ${enumeratedSchema::class.java.simpleName}")
                    }
                }

                referenceSchemas.isNotEmpty() -> {
                    subschemas.single() 
                    val referenceSchema = referenceSchemas.single()
                    val referredSchema = referenceSchema.referredSchema
                    referredSchema.toPropertyType(propertyName)
                }

                else -> {
                    error("Unsupported combined sub-schemas ${this.subschemas.map { it::class.java.simpleName }}")
                }
            }
        }
        else -> error("Unsupported property type ${this::class.java.simpleName}\n\t$this")
    }

private fun getEmbeddedJson(jsonId: String): JSONObject? {
    val embeddedDefinitions = getResourceDirectoryFiles("schemas/embedded")
    embeddedDefinitions.singleOrNull { it == jsonId }?.let {
        return getResourceJson("schemas/embedded/$it")
    }
    return null
}

private fun SchemaLocation.getClassName() =
    this.toString().split("/").lastOrNull()?.split(".")?.firstOrNull()