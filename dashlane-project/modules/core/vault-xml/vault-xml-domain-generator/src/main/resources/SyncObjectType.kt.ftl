package com.dashlane.xml.domain

import com.dashlane.xml.XmlData
<#list transactionTypes as transactionType>
import com.dashlane.xml.domain.SyncObject.${transactionType.className}
</#list>
import kotlin.reflect.KClass
import javax.annotation.processing.Generated

/**
 * A type of sync object and its properties, with a 1-1 correspondence to a [SyncObject] subclass.
 *
 * @property transactionType
 *  The name of the transactions as shown during the sync.
 * @property xmlObjectName
 *  The name of XML objects for this type.
 * @property kClass
 *  The corresponding [SyncObject] class.
 * @property conflictingFields
 *  The XML names of fields for which a conflict in an item of this type should lead to a duplication of the item in
 *  order to prevent critical data loss.
 * @property sensitiveFields
 *  The XML names of fields whose content should be obfuscated in memory.
 * @property hasDeduplication
 *  Whether objects of this type are candidates for deduplication. If this is `true`, then [SyncObject.userContentData]
 *  will always be non-null for the corresponding [SyncObject] subclass, otherwise it will always be null.
 */
@Generated
enum class SyncObjectType(
    val transactionType: String,
    val xmlObjectName: String,
    val kClass: KClass<out SyncObject>,
    val conflictingFields: Set<String> = emptySet(),
    val sensitiveFields: Set<String> = emptySet(),
    val hasDeduplication: Boolean = false
) {
    <#list transactionTypes as transactionType>
    ${transactionType.enumName}(
        SyncTransactionName.${transactionType.enumName},
        SyncObjectXmlName.${transactionType.enumName},
        ${transactionType.className}::class<#if transactionType.hasConflictingFields>,
        conflictingFields = SyncConflictDuplicates.${transactionType.fieldName}</#if><#if transactionType.hasSensitiveFields>,
        sensitiveFields = SyncSensitiveFields.${transactionType.fieldName}</#if><#if transactionType.hasUserContentFields>,
        hasDeduplication = true</#if>
    ) {
        override fun create(data: Map<String, XmlData>) =
            ${transactionType.className}(data)
    }<#sep>,
    </#list>
    ;<#lt>

    abstract fun create(data: Map<String, XmlData> = mapOf()): SyncObject

    companion object {
        private val byTransactionType: Map<String, SyncObjectType> =
            enumValues<SyncObjectType>().associateBy(SyncObjectType::transactionType).let {
                it + it.mapKeys { (key, _) -> key.replace("_", "") }
            }
        private val byXmlName: Map<String, SyncObjectType> =
            enumValues<SyncObjectType>().associateBy(SyncObjectType::xmlObjectName)
        private val byClass: Map<KClass<out SyncObject>, SyncObjectType> =
            enumValues<SyncObjectType>().associateBy(SyncObjectType::kClass)

        /**
         * @see [forTransactionTypeOrNull]
         */
        fun forTransactionType(transactionType: String): SyncObjectType =
            forTransactionTypeOrNull(transactionType)
                ?: throw IllegalArgumentException("SyncObjectType(transactionType = $transactionType) not found.")

        /**
         * @param transactionType
         *  The name of the object type, as found in either the `type` attribute of a transaction item returned by
         *  a `latest` call to the server, or in the `ObjectType` XML value of a data change history item.
         *  This method is tolerant to underscores which may be added in `type` by other clients, but aren't expected in
         *  the reference, and vice-versa.
         */
        fun forTransactionTypeOrNull(transactionType: String): SyncObjectType? =
            byTransactionType[transactionType]
                ?: byTransactionType[transactionType.replace("_", "")]

        /**
         * @see [forXmlNameOrNull]
         */
        fun forXmlName(xmlObjectName: String): SyncObjectType =
            forXmlNameOrNull(xmlObjectName)
                ?: throw IllegalArgumentException("SyncObjectType(xmlObjectName = $xmlObjectName) not found.")

        /**
         * @param xmlObjectName
         *  The name of the node with the item contents in XML.
         */
        fun forXmlNameOrNull(xmlObjectName: String): SyncObjectType? =
            byXmlName[xmlObjectName]

        fun forObject(syncObject: SyncObject): SyncObjectType =
            forClass(syncObject::class)

        fun forClass(kClass: KClass<out SyncObject>): SyncObjectType =
            byClass.getValue(kClass)
    }
}
