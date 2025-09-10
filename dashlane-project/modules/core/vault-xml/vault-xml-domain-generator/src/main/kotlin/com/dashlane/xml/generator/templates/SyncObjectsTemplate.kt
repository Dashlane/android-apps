package com.dashlane.xml.generator.templates

import com.dashlane.xml.model.TransactionDefinitions
import com.dashlane.xml.model.TransactionType
import com.dashlane.xml.model.VaultModel
import com.dashlane.xml.model.VaultModelRepository
import com.dashlane.xml.model.toTransactionTypeFromObject
import java.util.Locale

data class SyncObjectsTemplate(
    val baseType: TransactionType,
    val transactionTypes: List<TransactionType>,
) {
    val allTypes: List<TransactionType>
        get() = (transactionTypes + baseType).withXmlClassName()

    val allXmlTypes: List<TransactionType.InnerType>
        get() = (transactionTypes + transactionTypes.flatMap(TransactionType::innerTypes))
            .filterIsInstance<TransactionType.InnerType>()
            .sortedBy(XmlType::enclosedClassName)

    val conflictDuplicateTypes: List<XmlType>
        get() = transactionTypes.filter(TransactionType::hasConflictingFields).withXmlClassName()

    val sensitiveTypes: List<XmlType>
        get() = transactionTypes.filter(TransactionType::hasSensitiveFields).withXmlClassName()

    val userContentTypes: List<XmlType>
        get() = transactionTypes.filter(TransactionType::hasUserContentFields).withXmlClassName()

    val properties: List<PropertiesTemplate.Property>
        get() = baseType.properties

    val globalEnums: Set<TransactionType.EnumType>
        get() = (transactionTypes + transactionTypes.flatMap { it.innerJsonTypes }).flatMap { it.globalEnumTypes }.toSet()

    interface XmlType {
        val className: String
        val enumName: String
        val xmlName: String
        val properties: List<PropertiesTemplate.Property>
        val enclosingClassName: String?
            get() = null
        val enclosedClassName
            get() = if (enclosingClassName == null) className else "$enclosingClassName.$className"
    }

    data class TransactionType(
        override val className: String,
        override val enumName: String,
        override val xmlName: String,
        override val properties: List<PropertiesTemplate.Property>,
        val documentation: String,
        val transactionName: String,
        val fieldName: String,
        val conflictingFields: List<String> = emptyList(),
        val sensitiveFields: List<String> = emptyList(),
        val userContentFields: List<String> = emptyList(),
        val innerTypes: List<InnerType> = emptyList(),
        val innerJsonTypes: List<TransactionType> = emptyList(),
        val enumTypes: List<EnumType> = emptyList(),
        val globalEnumTypes: List<EnumType> = emptyList(),
        val defaultConstructor: DefaultConstructor?,
    ) : XmlType {
        val hasConflictingFields: Boolean
            get() = conflictingFields.isNotEmpty()

        val hasSensitiveFields: Boolean
            get() = sensitiveFields.isNotEmpty()

        val hasUserContentFields: Boolean
            get() = userContentFields.isNotEmpty()

        val hasDefaultConstructor: Boolean
            get() = defaultConstructor.let { it != null && it.isNotEmpty }

        data class InnerType(
            override val className: String,
            override val enumName: String,
            override val xmlName: String,
            override val properties: List<PropertiesTemplate.Property>,
            override val enclosingClassName: String,
            val enums: List<EnumType>,
            val toObject: Boolean,
        ) : XmlType

        data class EnumType(
            override val className: String,
            override val enumName: String,
            override val xmlName: String,
            override val properties: List<PropertiesTemplate.Property>,
        ) : XmlType

        data class DefaultConstructor(
            val requiredProperties: List<RequiredProperty>,
            val defaultProperties: List<DefaultProperty>,
        ) {
            val isNotEmpty
                get() = requiredProperties.isNotEmpty() || defaultProperties.isNotEmpty()

            data class RequiredProperty(
                val name: String,
                val type: String,
            )

            data class DefaultProperty(
                val name: String,
                val value: String,
            )
        }
    }
}

data class TransactionInnerTypeProperties(
    val name: String,
    val properties: Map<String, TransactionType.Property>,
    val type: TransactionType.Property.Type,
)

fun TransactionDefinitions.toSyncObjectsTemplate(): SyncObjectsTemplate =
    SyncObjectsTemplate(
        baseType = SyncObjectsTemplate.TransactionType(
            className = "SyncObject",
            enumName = "",
            xmlName = "",
            properties = baseProperties.map { it.toTemplateModelProperty() },
            documentation = "",
            transactionName = "",
            fieldName = "",
            userContentFields = baseProperties.filterValues { it.deduplicationSignature }.keys.map { it.toPascalCase() },
            defaultConstructor = null,
        ),
        transactionTypes = transactionTypes.toTemplateTransactionTypes(),
    )

private fun Map<VaultModel, TransactionType>.toTemplateTransactionTypes(): List<SyncObjectsTemplate.TransactionType> =
    map { (vaultModel, transactionType) ->
        transactionType.toTemplateTransactionType(vaultModel.name)
    }

@Suppress("LongMethod")
fun TransactionType.toTemplateTransactionType(className: String): SyncObjectsTemplate.TransactionType {
    return SyncObjectsTemplate.TransactionType(
        className = className,
        enumName = className.toPascalCase(),
        xmlName = xmlName,
        properties = toPropertiesTemplate(className).properties,
        documentation = description ?: "",
        transactionName = transactionName,
        fieldName = className.replaceFirstChar { it.lowercase(Locale.getDefault()) },
        conflictingFields = properties.filterValues { it.shouldDuplicateOnConflict }.keys.map { it.toPascalCase() },
        sensitiveFields = properties.filterValues { it.sensitive }.keys.map { it.toPascalCase() },
        userContentFields = properties.filterValues { it.deduplicationSignature }.keys.map { it.toPascalCase() },
        innerJsonTypes = innerJsonClasses.map {
            val vaultModelRepository = VaultModelRepository.loadJsonObject(it.data!!)
            val schema = vaultModelRepository.loadSchema(VaultModel("CustomJsonObject"))
            schema.toTransactionTypeFromObject().toTemplateTransactionType(it.name)
        },
        innerTypes = innerClasses.map {
            val innerClassName = it.name.dropXmlPrefix().snakeToUpperCamelCase()
            SyncObjectsTemplate.TransactionType.InnerType(
                className = innerClassName,
                enumName = innerClassName.toPascalCase(),
                xmlName = it.name,
                properties = it.properties.map { it.toTemplateModelProperty() },
                enclosingClassName = className,
                enums = it.properties.enumClasses.filterNot { it.isGlobal }.map { it.buildEnumType() },
                toObject = it.type is TransactionType.Property.Type.Object && it.type.isObject,
            )
        },
        enumTypes = properties.enumClasses.filterNot { it.isGlobal }.map { it.buildEnumType() },
        globalEnumTypes = globalEnums.map { it.buildEnumType() },
        defaultConstructor = SyncObjectsTemplate.TransactionType.DefaultConstructor(
            requiredProperties = properties.filterValues { it.required }
                .map { (name, property) ->
                    SyncObjectsTemplate.TransactionType.DefaultConstructor.RequiredProperty(
                        name = name.replaceFirstChar { it.lowercase(Locale.getDefault()) },
                        type = property.toKotlinType(),
                    )
                }
                .sortedBy { it.name },
            defaultProperties = properties.mapNotNull { (name, property) ->
                property.default?.let {
                    SyncObjectsTemplate.TransactionType.DefaultConstructor.DefaultProperty(
                        name = name.replaceFirstChar { it.lowercase(Locale.getDefault()) },
                        value = property.type.formatDefaultValue(it),
                    )
                }
            }.sortedBy { it.name },
        ),
    )
}

val TransactionType.innerClasses: List<TransactionInnerTypeProperties>
    get() = properties.values.map(TransactionType.Property::type)
        .mapNotNull(TransactionType.Property.Type::innerClass)

val TransactionType.innerJsonClasses: List<TransactionType.Property.Type.JsonData>
    get() = properties.filterValues { it.type is TransactionType.Property.Type.JsonData }
        .values.map { (it.type as TransactionType.Property.Type.JsonData) }
        .filter { it.data != null }

val TransactionType.globalEnums: List<TransactionType.Property.Type.StringEnum>
    get() = (properties.enumClasses + innerClasses.flatMap { it.properties.enumClasses }).filter { it.isGlobal }

val Map<String, TransactionType.Property>.enumClasses: List<TransactionType.Property.Type.StringEnum>
    get() = filterValues { it.type is TransactionType.Property.Type.StringEnum }.values
        .map { (it.type as TransactionType.Property.Type.StringEnum) }

val TransactionType.Property.Type.innerClass: TransactionInnerTypeProperties?
    get() = when (this) {
        is TransactionType.Property.Type.List -> type.innerClass
        is TransactionType.Property.Type.Object -> TransactionInnerTypeProperties(name, properties, this)
        else -> null
    }

private fun TransactionType.Property.toKotlinType() = when (type) {
    is TransactionType.Property.Type.Boolean ->
        "Boolean"
    is TransactionType.Property.Type.String ->
        if (sensitive) {
            "SyncSensitiveValue"
        } else {
            "String"
        }
    else -> error("Corresponding Kotlin type not implemented for ${this::class.java.simpleName}")
}

private fun TransactionType.Property.Type.formatDefaultValue(defaultValue: Any): String =
    when (this) {
        is TransactionType.Property.Type.Boolean ->
            when (defaultValue) {
                is Boolean -> defaultValue
                is Int -> defaultValue != 0
                is String -> when (defaultValue) {
                    "true", "YES" -> true
                    "false", "NO" -> false
                    else -> error("Unexpected default boolean value $defaultValue.")
                }
                else -> error("Unexpected default boolean value $defaultValue.")
            }.toString()
        is TransactionType.Property.Type.String,
        is TransactionType.Property.Type.StringEnum,
        ->
            "\"${defaultValue as String}\""
        else -> error("Default value formatting not implemented for ${this::class.java.simpleName}")
    }

private fun List<SyncObjectsTemplate.TransactionType>.withXmlClassName() =
    map {
        
        it.copy(className = it.className + "Xml")
    }.sortedBy { it.className }

fun String.sanitizeEnumName() = this.replace("-", "_").uppercase(Locale.ROOT)

fun TransactionType.Property.Type.StringEnum.buildEnumType() =
    SyncObjectsTemplate.TransactionType.EnumType(
        className = name.snakeToUpperCamelCase(),
        enumName = "",
        xmlName = "",
        properties = values.map { value ->
            PropertiesTemplate.Property(
                documentation = "",
                name = value.sanitizeEnumName(),
                keyValue = value,
                keyName = value,
                type = PropertiesTemplate.Property.Type.String,
            )
        },
    )