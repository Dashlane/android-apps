package com.dashlane.sync.xml

import com.dashlane.xml.XmlData



fun XmlData.ObjectNode.mergeInto(
    other: XmlData.ObjectNode?,
    anonymousListsStrategy: MergeListStrategy = MergeListStrategy.DEDUPLICATE
): XmlData.ObjectNode {
    other ?: return this
    if (other.type != this.type) throw IllegalStateException("Tried to merge a '$type' node into a '${other.type}' node.")

    return copy(
        data = data.mergeInto(other.data, anonymousListsStrategy)
    )
}



private fun Map<String, XmlData>.mergeInto(
    others: Map<String, XmlData>,
    anonymousListsStrategy: MergeListStrategy = MergeListStrategy.DEDUPLICATE
): Map<String, XmlData> {
    return (this - others.keys) + (others - this.keys) + (this.keys.intersect(others.keys)).associateWith { key ->
        val otherValue = others[key]!!
        val thisValue = this[key]!!
        when (otherValue) {
            is XmlData.ItemNode -> {
                check(thisValue is XmlData.ItemNode) { "Tried to merge '$thisValue' into an item node." }
                thisValue
            }
            is XmlData.ItemNodeObfuscated -> {
                check(thisValue is XmlData.ItemNodeObfuscated) {
                    "Tried to merge '$thisValue' into an item node sensitive."
                }
                thisValue
            }
            is XmlData.ListNode -> {
                check(thisValue is XmlData.ListNode) { "Tried to merge '$thisValue' into a list node." }
                thisValue.mergeInto(otherValue, anonymousListsStrategy)
            }
            is XmlData.CollectionNode -> {
                check(thisValue is XmlData.CollectionNode) { "Tried to merge '$thisValue' into collection node." }
                XmlData.CollectionNode(thisValue.value.mergeInto(otherValue.value))
            }
            is XmlData.ObjectNode -> {
                check(thisValue is XmlData.ListNode) { "Tried to merge '$thisValue' into a ${otherValue.type} node." }
                thisValue.mergeInto(otherValue as XmlData.ListNode, anonymousListsStrategy)
            }
        }
    }
}

fun XmlData.ListNode.mergeInto(
    other: XmlData.ListNode,
    anonymousListsStrategy: MergeListStrategy
): XmlData.ListNode = when (anonymousListsStrategy) {
    MergeListStrategy.DEDUPLICATE -> XmlData.ListNode((other.value.toSet() + value).toList())
    MergeListStrategy.KEEP_RICHEST -> {
        val mutableL1 = value.toMutableList()
        val mutableL2 = other.value.toMutableList()
        val result = mutableL1.mergeRichestInto(mutableL2)
        XmlData.ListNode(result)
    }
}

private fun MutableList<XmlData>.mergeRichestInto(
    mutableL2: MutableList<XmlData>
): MutableList<XmlData> {
    val result = mutableListOf<XmlData>()

    while (isNotEmpty() || mutableL2.isNotEmpty()) {
        if (isEmpty()) {
            result.addAll(mutableL2)
            mutableL2.clear()
            continue
        }
        if (mutableL2.isEmpty()) {
            result.addAll(this)
            clear()
            continue
        }

        val first1 = first()
        val contained2 = mutableL2.firstOrNull { first1.contains(it) }
        if (contained2 != null) {
            mutableL2.remove(contained2)
            remove(first1)
            result.add(first1)
        } else {
            val first2 = mutableL2.first()
            val contained1 = firstOrNull {
                first2.contains(it)
            }
            if (contained1 != null) {
                remove(contained1)
                mutableL2.remove(first2)
                result.add(first2)
            } else {
                removeAt(0)
                mutableL2.removeAt(0)
                result.add(first1)
                result.add(first2)
            }
        }
    }
    return result
}

fun XmlData.contains(other: XmlData): Boolean {
    return when {
        this is XmlData.ItemNode && other is XmlData.ItemNode -> this == other
        this is XmlData.ListNode && other is XmlData.ListNode ->
            other.value.all { otherSon -> value.any { it.contains(otherSon) } }
        this is XmlData.CollectionNode && other is XmlData.CollectionNode ->
            other.value.all { (key, otherValue) ->
                value[key]?.contains(otherValue) ?: false
            }
        this is XmlData.ObjectNode && other is XmlData.ObjectNode ->
            type == other.type && other.data.all { (key, otherValue) ->
                data[key]?.contains(otherValue) ?: false
            }
        else -> false
    }
}