@file:Suppress("kotlin:S6514")

package com.dashlane.xml

import com.dashlane.xml.utils.filterValuesInstanceOf
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException

sealed class XmlData {
    abstract fun isEmpty(): Boolean

    data class ItemNode(val value: String) : XmlData() {
        override fun isEmpty(): Boolean = value.isEmpty()
    }

    data class ItemNodeObfuscated(val value: XmlObfuscatedValue) : XmlData() {
        constructor(value: String) : this(XmlObfuscatedValue(value))

        override fun isEmpty(): Boolean = value.isEmpty()
    }

    data class ListNode(
        val value: List<XmlData> = emptyList(),
    ) : XmlData(), List<XmlData> by value {
        constructor(vararg values: XmlData) : this(values.asList())

        override fun isEmpty(): Boolean = value.isEmpty()
    }

    data class CollectionNode(
        val value: Map<String, XmlData> = emptyMap(),
    ) : XmlData(), Map<String, XmlData> by value {
        constructor(vararg values: Pair<String, XmlData>) : this(values.toMap())

        override fun isEmpty(): Boolean = value.isEmpty()
    }

    data class ObjectNode(
        val type: String,
        val data: Map<String, XmlData> = emptyMap(),
    ) : XmlData(), Map<String, XmlData> by data {
        constructor(type: String, vararg data: Pair<String, XmlData>) : this(type, data.toMap())

        override fun isEmpty(): Boolean = data.isEmpty()
    }
}

fun XmlObfuscatedValue.toItemNodeObfuscated(): XmlData.ItemNodeObfuscated =
    XmlData.ItemNodeObfuscated(this)

fun String.toItemNode(): XmlData.ItemNode =
    XmlData.ItemNode(this)

fun List<XmlData>.toListNode(): XmlData.ListNode =
    XmlData.ListNode(this)

fun Map<String, XmlData>.toCollectionNode(): XmlData.CollectionNode =
    XmlData.CollectionNode(this)

fun Map<String, XmlData>.toObjectNode(type: String): XmlData.ObjectNode =
    XmlData.ObjectNode(type, this)

inline fun <reified T> T.toItemNodeFromJson(): XmlData.ItemNode =
    XmlData.ItemNode(
        Moshi.Builder().build().adapter(T::class.java).toJson(this),
    )

inline fun <reified T> List<T>.toItemListNodeFromJson(): XmlData.ItemNode {
    val moshi = Moshi.Builder()
        .build()
        .adapter(T::class.java)
    val json = this.map { moshi.toJson(it) }
    return XmlData.ItemNode(json.toString())
}

fun SyncObjectEnum.toItemNodeFromEnum(): XmlData.ItemNode {
    return XmlData.ItemNode(this.value)
}

fun XmlData.asItemValue(): String =
    asItemNode().value

fun XmlData.asItemNode(): XmlData.ItemNode =
    asItemNodeOrNull() ?: error("Expected ${XmlData.ItemNode::class.java.simpleName}, was $this")

fun XmlData.asItemValueOrNull(): String? =
    asItemNodeOrNull()?.value

inline fun <reified T> XmlData.asJsonDataClassOrNull(): T? {
    val itemValue = asItemValue()
    if (itemValue.isBlank()) {
        return null
    }

    return try {
        Moshi.Builder().build().adapter(T::class.java).fromJson(itemValue)
    } catch (_: JsonDataException) {
        null
    } catch (_: IOException) {
        null
    }
}

inline fun <reified T> XmlData.asJsonDataListClassOrNull(): List<T>? {
    val itemValue = asItemValue()
    if (itemValue.isBlank()) {
        return null
    }

    return try {
        Moshi.Builder()
            .build()
            .adapter<List<T>>(Types.newParameterizedType(List::class.java, T::class.java))
            .fromJson(itemValue)
    } catch (e: JsonDataException) {
        null
    } catch (_: IOException) {
        null
    }
}

inline fun <reified T : SyncObjectEnum> XmlData.asEnumValue(): T? {
    return SyncObjectEnum.getEnumForValue(asItemValue())
}

fun XmlData.asItemNodeOrNull(): XmlData.ItemNode? =
    this as? XmlData.ItemNode

fun XmlData.asItemObfuscatedValue(): XmlObfuscatedValue =
    asItemNodeObfuscated().value

fun XmlData.asItemNodeObfuscated(): XmlData.ItemNodeObfuscated =
    asItemNodeObfuscatedOrNull()
        ?: error("Expected ${XmlData.ItemNodeObfuscated::class.java.simpleName}, was $this")

fun XmlData.asItemNodeObfuscatedValueOrNull(): XmlObfuscatedValue? =
    asItemNodeObfuscatedOrNull()?.value

fun XmlData.asItemNodeObfuscatedOrNull(): XmlData.ItemNodeObfuscated? =
    this as? XmlData.ItemNodeObfuscated

fun XmlData.asListValue(): List<XmlData> =
    asListNode().value

fun XmlData.asListNode(): XmlData.ListNode =
    asListNodeOrNull() ?: error("Expected ${XmlData.ListNode::class.java.simpleName}, was $this")

fun XmlData.asListValueOrNull() =
    asListNodeOrNull()?.value

fun XmlData.asListNodeOrNull(): XmlData.ListNode? =
    this as? XmlData.ListNode

fun XmlData.asCollectionValue(): Map<String, XmlData> =
    asCollectionNode().value

fun XmlData.asCollectionNode(): XmlData.CollectionNode =
    asCollectionNodeOrNull()
        ?: error("Expected ${XmlData.CollectionNode::class.java.simpleName}, was $this")

fun XmlData.asCollectionValueOrNull(): Map<String, XmlData>? =
    asCollectionNodeOrNull()?.value

fun XmlData.asCollectionNodeOrNull(): XmlData.CollectionNode? =
    this as? XmlData.CollectionNode

inline fun <T> XmlData.asObject(type: String, factory: (Map<String, XmlData>) -> T): T =
    asObjectNode(type).let(factory)

inline fun <T> XmlData.asObjectOrNull(type: String, factory: (Map<String, XmlData>) -> T): T? =
    asObjectNodeOrNull(type)?.let(factory)

fun XmlData.asObjectNode(type: String): XmlData.ObjectNode =
    asObjectNodeOrNull(type)
        ?: error("Expected ${XmlData.CollectionNode::class.java.simpleName}(type = $type), was $this")

fun XmlData.asObjectNode(): XmlData.ObjectNode =
    asObjectNodeOrNull()
        ?: error("Expected ${XmlData.ObjectNode::class.java.simpleName}, was $this")

fun XmlData.asObjectNodeOrNull(type: String): XmlData.ObjectNode? =
    asObjectNodeOrNull()?.takeIf { it.type == type }

fun XmlData.asObjectNodeOrNull(): XmlData.ObjectNode? =
    this as? XmlData.ObjectNode

fun Map<String, XmlData>.filterItemNodes(): Map<String, XmlData.ItemNode> =
    filterValuesInstanceOf()

fun Map<String, XmlData>.filterListNodes(): Map<String, XmlData.ListNode> =
    filterValuesInstanceOf()

fun Map<String, XmlData>.filterCollectionNodes(): Map<String, XmlData.CollectionNode> =
    filterValuesInstanceOf()

fun Map<String, XmlData>.filterObjectNodes(type: String): Map<String, XmlData.ObjectNode> =
    filterObjectNodes().filterValues { it.type == type }

fun Map<String, XmlData>.filterObjectNodes(): Map<String, XmlData.ObjectNode> =
    filterValuesInstanceOf()