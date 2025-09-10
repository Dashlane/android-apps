package com.dashlane.xml.domain

import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.XmlData
import com.dashlane.xml.asCollectionValueOrNull
import com.dashlane.xml.asEnumValue
import com.dashlane.xml.asItemNodeObfuscatedValueOrNull
import com.dashlane.xml.asItemValueOrNull
import com.dashlane.xml.asJsonDataClassOrNull
import com.dashlane.xml.asJsonDataListClassOrNull
import com.dashlane.xml.asListValueOrNull
<#list allTypes as transactionType>
import com.dashlane.xml.domain.properties.${transactionType.className}
</#list>
import com.dashlane.xml.domain.time.toInstantOrNull
import com.dashlane.xml.domain.time.toLocalDateOrNull
import com.dashlane.xml.domain.time.toMonthValueOrNull
import com.dashlane.xml.domain.time.toXmlString
import com.dashlane.xml.domain.time.toYearValueOrNull
import com.dashlane.xml.domain.utils.Country
import com.dashlane.xml.domain.utils.decodeBase64
import com.dashlane.xml.domain.utils.encodeBase64
import com.dashlane.xml.domain.utils.mapValuesNotNull
import com.dashlane.xml.domain.utils.set
import com.dashlane.xml.domain.utils.toBooleanValue
import com.dashlane.xml.domain.utils.toCountryOrNull
import com.dashlane.xml.domain.utils.toDoubleValueOrNull
import com.dashlane.xml.domain.utils.toLongValueOrNull
import com.dashlane.xml.toCollectionNode
import com.dashlane.xml.toItemNode
import com.dashlane.xml.toItemNodeFromEnum
import com.dashlane.xml.toItemNodeFromJson
import com.dashlane.xml.toItemListNodeFromJson
import com.dashlane.xml.toItemNodeObfuscated
import com.dashlane.xml.toListNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.Year
import javax.annotation.processing.Generated

@Suppress(
    "LargeClass",
    "ktlint:standard:max-line-length",
)
@Generated
sealed class SyncObject {
    abstract val data: Map<String, XmlData>
    abstract val userContentData: Map<String, XmlData>?

    <#list properties as property>
    /**
     * ${property.documentation}
     */
    val ${property.name}: ${property.type.name}?
        get() = data[SyncObjectXml.${property.keyName}]?.${property.type.xmlGetter}
    <#sep>

    </#list>

    private val builderData: MutableMap<String, XmlData>
        get() = data.toMutableMap()

    abstract fun builder(data: MutableMap<String, XmlData> = builderData): Builder

    <#lt>
    <#list transactionTypes as transactionType>
    /**
     * ${transactionType.documentation}
     */
    @Generated
    data class ${transactionType.className}(override val data: Map<String, XmlData>) : SyncObject() {

        override val userContentData: Map<String, XmlData><#if !transactionType.hasUserContentFields>?</#if>
            get() = <#if transactionType.hasUserContentFields>data.filterKeys { it in SyncUserContentFields.${transactionType.fieldName} }.filterValues { !it.isEmpty() }<#else>null</#if>

        <#list transactionType.properties as property>
        <#if property.documentation != "">
        /**
         * ${property.documentation}
         */
        </#if>
        val ${property.name}: ${property.type.name}?
            get() = data[${transactionType.className}Xml.${property.keyName}]?.${property.type.xmlGetter}
        <#sep>

        </#list>

        inline fun copy(builder: Builder.() -> Unit = {}): ${transactionType.className} =
            builder().apply(builder).build()

        override fun builder(data: MutableMap<String, XmlData>) =
            Builder(data)

        companion object {
            <#if transactionType.hasDefaultConstructor>
            fun create(
                <#list transactionType.defaultConstructor.requiredProperties as requiredProperty>
                ${requiredProperty.name}: ${requiredProperty.type}<#sep>,
                </#list>

            ) = ${transactionType.className} {
                <#list transactionType.defaultConstructor.requiredProperties as requiredProperty>
                this.${requiredProperty.name} = ${requiredProperty.name}
                </#list>
                <#list transactionType.defaultConstructor.defaultProperties as defaultProperty>
                this.${defaultProperty.name} = ${defaultProperty.value}
                </#list>
            }

            </#if>
            inline operator fun invoke(builder: Builder.() -> Unit = {}): ${transactionType.className} =
                Builder().apply(builder).build()
        }

        @Generated
        @SyncObjectBuilder
        class Builder(data: MutableMap<String, XmlData> = mutableMapOf()) : SyncObject.Builder(data) {
            <#list transactionType.properties as property>
            var ${property.name}: ${property.type.name}?
                get() = data[${transactionType.className}Xml.${property.keyName}]?.${property.type.xmlGetter}
                set(value) {
                    data[${transactionType.className}Xml.${property.keyName}] = value?.${property.type.xmlSetter}
                }
            <#sep>

            </#list>

            override fun build(): ${transactionType.className} =
                ${transactionType.className}(data.toMap())
        }
        <#list transactionType.innerTypes>

        <#items as innerType>
        @Generated
        data class ${innerType.className}(val data: Map<String, XmlData>) {

            <#list innerType.properties as property>
            <#if property.documentation != "">
            /**
             * ${property.documentation}
             */
            </#if>
            val ${property.name}: ${property.type.name}?
                get() = data[${transactionType.className}Xml.${innerType.className}Xml.${property.keyName}]?.${property.type.xmlGetter}
            <#sep>

            </#list>

            private val builderData: MutableMap<String, XmlData>
                get() = data.toMutableMap()

            inline fun copy(builder: Builder.() -> Unit = {}): ${innerType.className} =
                builder().apply(builder).build()

            fun builder(data: MutableMap<String, XmlData> = builderData) =
                Builder(data)

            companion object {
                inline operator fun invoke(builder: Builder.() -> Unit = {}): ${innerType.className} =
                    Builder().apply(builder).build()
            }

            @SyncObjectBuilder
            class Builder(val data: MutableMap<String, XmlData> = mutableMapOf()) {
                <#list innerType.properties as property>
                var ${property.name}: ${property.type.name}?
                    get() = data[${transactionType.className}Xml.${innerType.className}Xml.${property.keyName}]?.${property.type.xmlGetter}
                    set(value) {
                        data[${transactionType.className}Xml.${innerType.className}Xml.${property.keyName}] = value?.${property.type.xmlSetter}
                    }
                <#sep>

                </#list>

                fun build(): ${innerType.className} =
                    ${innerType.className}(data.toMap())
            }
        }
        <#list innerType.enums>

        <#items as enumTypes>
        enum class ${enumTypes.className}(override val value: String) : SyncObjectEnum {
            <#list enumTypes.properties as property>
            ${property.name}("${property.keyName}"),
            </#list>
        }
        </#items>
        </#list>
        </#items>
        </#list>
        <#list transactionType.enumTypes>

        <#items as enumTypes>
        enum class ${enumTypes.className}(override val value: String) : SyncObjectEnum {
            <#list enumTypes.properties as property>
            @Json(name = "${property.keyName}")
            ${property.name}("${property.keyName}"),
            <#sep>

            </#list>
        }
        <#sep>

        </#items>
        </#list>
        <#list transactionType.innerJsonTypes>
        <#items as jsonType>
        @Generated
        @JsonClass(generateAdapter = true)
        data class ${jsonType.className}(
        <#list jsonType.properties as property>
            <#if property.documentation != "">
            /**
             * ${property.documentation}
             */
            </#if>
            @Json(name = "${property.keyValue}")
            val ${property.name}: ${property.type.name}?,
            <#sep>

        </#list>
        )<#list jsonType.innerTypes><#rt>
         <#lt> {
            <#items as innerClass>
            @Generated
            @JsonClass(generateAdapter = true)
            data class ${innerClass.className}(
            <#list innerClass.properties as property>
                <#if property.documentation != "">
                /**
                 * ${property.documentation}
                 */
                </#if>
                @Json(name = "${property.keyValue}")
                val ${property.name}: ${property.type.name}?,
                <#sep>

            </#list>
            )<#list innerClass.enums><#rt>
            <#lt> {
            <#items as enumTypes>
                enum class ${enumTypes.className}(override val value: String) : SyncObjectEnum {
                    <#list enumTypes.properties as property>
                    @Json(name = "${property.keyName}")
                    ${property.name}("${property.keyName}"),
                        <#sep>

                    </#list>
                }
             </#items>
            }
             <#sep>

             </#list>
             </#items>

        }
        </#list>

        </#items>
        </#list>
    }
    <#sep>

    </#list>

    abstract class Builder internal constructor(val data: MutableMap<String, XmlData> = mutableMapOf()) {
        <#list properties as property>
        var ${property.name}: ${property.type.name}?
            get() = data[SyncObjectXml.${property.keyName}]?.${property.type.xmlGetter}
            set(value) {
                data[SyncObjectXml.${property.keyName}] = value?.${property.type.xmlSetter}
            }
        <#sep>

        </#list>
        abstract fun build(): SyncObject
    }
    <#list globalEnums>

    <#items as enumTypes>
    enum class ${enumTypes.className}(override val value: String) : SyncObjectEnum {
        <#list enumTypes.properties as property>
        @Json(name = "${property.keyName}")
        ${property.name}("${property.keyName}"),
        <#sep>

        </#list>
    }
    <#sep>

    </#items>
    </#list>
}
