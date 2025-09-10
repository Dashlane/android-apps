@file:Suppress("kotlin:S2097")

package com.dashlane.xml.generator.templates

import com.dashlane.xml.model.TransactionType
import java.util.Locale

data class PropertiesTemplate(
    val className: String,
    val properties: List<Property>,
    val imports: List<String> = emptyList(),
    val innerClasses: List<PropertiesTemplate> = emptyList(),
    val xmlConstantName: String = "",
) {
    data class Property(
        val documentation: String,
        val name: String,
        val keyValue: String,
        val keyName: String,
        val type: Type,
    ) {
        open class Type(
            val name: kotlin.String,
            val xmlGetterCalls: kotlin.collections.List<kotlin.String>,
            val xmlSetterCalls: kotlin.collections.List<kotlin.String>,
        ) {
            val xmlGetter: kotlin.String
                get() = xmlGetterCalls.joinToString("?.")

            val xmlSetter: kotlin.String
                get() = xmlSetterCalls.joinToString("?.")

            abstract class Primitive(
                name: kotlin.String,
                fromStringConverter: kotlin.String? = null,
                toStringConverter: kotlin.String? = "toString()",
            ) : Type(
                    name = name,
                    xmlGetterCalls = listOfNotNull("asItemValueOrNull()", fromStringConverter),
                    xmlSetterCalls = listOfNotNull(toStringConverter, "toItemNode()"),
                )

            object Boolean : Primitive(name = "Boolean", fromStringConverter = "toBooleanValue()")

            object String : Primitive(name = "String", toStringConverter = null)

            object Long : Primitive(name = "Long", fromStringConverter = "toLongValueOrNull()")

            object Double :
                Primitive(name = "Double", fromStringConverter = "toDoubleValueOrNull()")

            object Month : Primitive(
                name = "Month",
                fromStringConverter = "toMonthValueOrNull()",
                toStringConverter = TO_XML_STRING,
            )

            object Year : Primitive(
                name = "Year",
                fromStringConverter = "toYearValueOrNull()",
                toStringConverter = TO_XML_STRING,
            )

            object LocalDate : Primitive(
                name = "LocalDate",
                fromStringConverter = "toLocalDateOrNull()",
                toStringConverter = TO_XML_STRING,
            )

            object SyncObfuscatedValue : Type(
                name = "SyncObfuscatedValue",
                xmlGetterCalls = listOfNotNull("asItemNodeObfuscatedValueOrNull()"),
                xmlSetterCalls = listOfNotNull("toItemNodeObfuscated()"),
            )

            object Instant : Primitive(
                name = "Instant",
                fromStringConverter = "toInstantOrNull()",
                toStringConverter = TO_XML_STRING,
            )

            object Base64EncodedByteArray : Primitive(
                name = "ByteArray",
                fromStringConverter = "decodeBase64()",
                toStringConverter = "encodeBase64()",
            )

            object Country : Primitive(
                name = "Country",
                fromStringConverter = "toCountryOrNull()",
                toStringConverter = "isoCode",
            )

            abstract class List(
                genericType: Type,
            ) : Type(
                    name = "List<${genericType.name}>",
                    xmlGetterCalls = listOf(
                        "asListValueOrNull()",
                        "mapNotNull { it.${genericType.xmlGetterCalls.joinToString(".")} }",
                    ),
                    xmlSetterCalls = listOf("map { it.${genericType.xmlSetterCalls.joinToString(".")} }?.toListNode()"),
                )

            object StringList : List(
                genericType = String,
            )

            object StringMapList : List(
                genericType = StringMap,
            )

            class CollectionList(genericType: InnerClass) : List(
                genericType = genericType,
            ) {
                override fun equals(other: Any?): kotlin.Boolean {
                    if (other is ObjectList) {
                        return other.name == name
                    }
                    return false
                }

                override fun hashCode(): Int {
                    return javaClass.hashCode()
                }
            }

            class ObjectList(genericType: InnerClassObject) : List(
                genericType = genericType,
            ) {
                override fun equals(other: Any?): kotlin.Boolean {
                    if (other is ObjectList) {
                        return other.name == name
                    }
                    return false
                }

                override fun hashCode(): Int {
                    return javaClass.hashCode()
                }
            }

            object StringMap : Type(
                name = "Map<String, String>",
                xmlGetterCalls = listOf("asCollectionValueOrNull()?.mapValuesNotNull { (_, value) -> value.asItemValueOrNull() }"),
                xmlSetterCalls = listOf(
                    "mapValues { (_, it) -> it.toItemNode() }",
                    "toCollectionNode()",
                ),
            )

            class InnerClass(name: kotlin.String) : Type(
                name = name.snakeToUpperCamelCase(),
                xmlGetterCalls = listOf("as${name}OrNull()"),
                xmlSetterCalls = listOf("toCollectionNode()"),
            ) {
                override fun equals(other: Any?): kotlin.Boolean {
                    if (other is ObjectList) {
                        return other.name == name
                    }
                    return false
                }

                override fun hashCode(): Int {
                    return javaClass.hashCode()
                }
            }

            class InnerClassObject(name: kotlin.String) : Type(
                name = name.snakeToUpperCamelCase(),
                xmlGetterCalls = listOf("as${name}OrNull()"),
                xmlSetterCalls = listOf("toObjectNode()"),
            ) {
                override fun equals(other: Any?): kotlin.Boolean {
                    if (other is ObjectList) {
                        return other.name == name
                    }
                    return false
                }

                override fun hashCode(): Int {
                    return javaClass.hashCode()
                }
            }

            class JsonInnerClass(name: kotlin.String) : Type(
                name = name,
                xmlGetterCalls = listOf("asJsonDataClassOrNull()"),
                xmlSetterCalls = listOf("toItemNodeFromJson()"),
            )

            class JsonListInnerClass(name: kotlin.String) : Type(
                name = "List<$name>",
                xmlGetterCalls = listOf("asJsonDataListClassOrNull()"),
                xmlSetterCalls = listOf("toItemListNodeFromJson()"),
            )

            data class EnumClass(val enumName: kotlin.String) : Type(
                name = enumName,
                xmlGetterCalls = listOf("asEnumValue()"),
                xmlSetterCalls = listOf("toItemNodeFromEnum()"),
            )
        }
    }

    companion object {
        private const val TO_XML_STRING = "toXmlString()"
    }
}

fun TransactionType.toPropertiesTemplate(className: String): PropertiesTemplate {
    val properties = this.properties.map { it.toTemplateModelProperty() }
    val innerClasses = innerClasses.map { (xmlName, properties) ->
        val innerClassName = xmlName.dropXmlPrefix()
        PropertiesTemplate(
            innerClassName,
            properties.map { (key, value) ->
                value.toTemplateModelProperty(
                    key,
                    value.description,
                )
            },
            xmlConstantName = innerClassName.toPascalCase(),
        )
    }
    val imports =
        listOf(
            "com.dashlane.xml.domain.VaultObject",
            "com.dashlane.xml.domain.VaultObject.$className",
            "com.dashlane.xml.domain.VaultObjectBuilder",
        ) + innerClasses.map { "com.dashlane.xml.domain.VaultObject.$className.${it.className}" }
    return PropertiesTemplate(
        className = className,
        properties = properties,
        imports = imports.sorted(),
        innerClasses = innerClasses,
    )
}

fun Map.Entry<String, TransactionType.Property>.toTemplateModelProperty() =
    value.toTemplateModelProperty(key, value.description)

private fun TransactionType.Property.toTemplateModelProperty(name: String, description: String?) =
    PropertiesTemplate.Property(
        documentation = description ?: "",
        name = name.snakeToUpperCamelCase().replaceFirstChar { it.lowercase(Locale.ROOT) },
        keyValue = name,
        keyName = name.toPascalCase(),
        type = toPropertyType(),
    )

private fun TransactionType.Property.toPropertyType(): PropertiesTemplate.Property.Type =
    type.toPropertyType(sensitive)


@Suppress("ComplexMethod")
private fun TransactionType.Property.Type.toPropertyType(sensitive: Boolean): PropertiesTemplate.Property.Type =
    when (this) {
        is TransactionType.Property.Type.Boolean ->
            PropertiesTemplate.Property.Type.Boolean
        is TransactionType.Property.Type.StringEnum ->
            PropertiesTemplate.Property.Type.EnumClass(name.snakeToUpperCamelCase())
        is TransactionType.Property.Type.String ->
            toPropertyTypeString(sensitive)
        is TransactionType.Property.Type.Integer ->
            PropertiesTemplate.Property.Type.Long
        is TransactionType.Property.Type.Float ->
            PropertiesTemplate.Property.Type.Double
        is TransactionType.Property.Type.Month ->
            PropertiesTemplate.Property.Type.Month
        is TransactionType.Property.Type.Year ->
            PropertiesTemplate.Property.Type.Year
        is TransactionType.Property.Type.LocalDate ->
            PropertiesTemplate.Property.Type.LocalDate
        is TransactionType.Property.Type.Instant ->
            PropertiesTemplate.Property.Type.Instant
        is TransactionType.Property.Type.Base64EncodedByteArray ->
            PropertiesTemplate.Property.Type.Base64EncodedByteArray
        is TransactionType.Property.Type.Country ->
            PropertiesTemplate.Property.Type.Country
        is TransactionType.Property.Type.List -> toPropertyTypeList(sensitive)
        is TransactionType.Property.Type.Object -> toPropertyTypeObject()
        is TransactionType.Property.Type.JsonData -> {
            if (this.data?.get("type") == "array") {
                PropertiesTemplate.Property.Type.JsonListInnerClass(this.name.dropXmlPrefix())
            } else {
                PropertiesTemplate.Property.Type.JsonInnerClass(name)
            }
        }
    }

private fun toPropertyTypeString(sensitive: Boolean) =
    if (sensitive) {
        PropertiesTemplate.Property.Type.SyncObfuscatedValue
    } else {
        PropertiesTemplate.Property.Type.String
    }

private fun TransactionType.Property.Type.List.toPropertyTypeList(sensitive: Boolean) =
    when (val genericType = type.toPropertyType(sensitive)) {
        is PropertiesTemplate.Property.Type.String -> PropertiesTemplate.Property.Type.StringList
        is PropertiesTemplate.Property.Type.StringMap -> PropertiesTemplate.Property.Type.StringMapList
        is PropertiesTemplate.Property.Type.InnerClass -> PropertiesTemplate.Property.Type.CollectionList(
            genericType,
        )
        is PropertiesTemplate.Property.Type.InnerClassObject -> PropertiesTemplate.Property.Type.ObjectList(
            genericType,
        )
        else -> error("List of ${genericType::name} not implemented")
    }

private fun TransactionType.Property.Type.Object.toPropertyTypeObject(): PropertiesTemplate.Property.Type {
    return if (this.properties.isEmpty()) {
        
        PropertiesTemplate.Property.Type.StringMap
    } else {
        if (this.isObject) {
            PropertiesTemplate.Property.Type.InnerClassObject(this.name.dropXmlPrefix())
        } else {
            PropertiesTemplate.Property.Type.InnerClass(this.name.dropXmlPrefix())
        }
    }
}

fun String.dropXmlPrefix(): String = if (startsWith("KW")) drop("KW".length) else this

fun String.toPascalCase() =
    replaceFirstChar { it.lowercase(Locale.getDefault()) }
        .replace(Regex("[A-Z]")) { "_${it.value}" }
        .uppercase(Locale.US)

fun String.snakeToLowerCamelCase(): String {
    return "_[a-zA-Z0-9]".toRegex().replace(this) {
        it.value.replace("_", "")
            .uppercase(Locale.ROOT)
    }
}

fun String.snakeToUpperCamelCase(): String {
    return this.snakeToLowerCamelCase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}