package com.dashlane.autofill.api.securitywarnings.data

import com.dashlane.autofill.api.securitywarnings.model.Item
import com.dashlane.autofill.api.securitywarnings.model.Source
import com.google.gson.annotations.SerializedName

internal data class SecurityWarningsJson(
    @SerializedName("securityWarningType")
    val securityWarningType: String,

    @SerializedName("signatures")
    private val signatures: List<String> = emptyList(),

    @SerializedName("itemsUrlRoot")
    private val itemsUrlRoot: List<String> = emptyList(),

    @SerializedName("itemsIds")
    private val itemsIds: List<String> = emptyList(),

    @SerializedName("apps")
    private val apps: List<String> = emptyList(),

    @SerializedName("pages")
    private val pages: List<List<String>> = emptyList(),

    @SerializedName("itemsIdApps")
    private val itemsIdApps: List<List<Int>> = emptyList(),

    @SerializedName("itemsIdPages")
    private val itemsIdPages: List<List<Int>> = emptyList(),

    @SerializedName("itemsUrlRootApps")
    private val itemsUrlRootApps: List<List<Int>> = emptyList(),

    @SerializedName("itemsUrlRootPages")
    private val itemsUrlRootPages: List<List<Int>> = emptyList(),

    @SerializedName("usedApps")
    private val usedApps: List<List<Int>> = emptyList()
) {
    fun has(signature: String, source: Source): Boolean {
        val relation = sourceRelation(signature, source) ?: return false

        return usedApps.contains(relation)
    }

    fun has(signature: String, item: Item, source: Source): Boolean {
        val relation = itemSourceRelation(signature, item, source) ?: return false

        return when {
            item is Item.SoftMatchItem && source is Source.App -> itemsIdApps.contains(relation)
            item is Item.SoftMatchItem && source is Source.Page -> itemsIdPages.contains(relation)
            item is Item.UrlMatchItem && source is Source.App -> itemsUrlRootApps.contains(relation)
            item is Item.UrlMatchItem && source is Source.Page -> itemsUrlRootPages.contains(relation)
            else -> false
        }
    }

    fun add(signature: String, source: Source): SecurityWarningsJson? {
        val dataUpdatedJson = insertSignature(signature)
            .insertSource(source)

        val dataRelation = dataUpdatedJson.sourceRelation(signature, source) ?: return null

        return dataUpdatedJson.copy(usedApps = usedApps.plusElement(dataRelation))
    }

    fun add(signature: String, item: Item, source: Source): SecurityWarningsJson? {
        val updatedJson = insertSignature(signature)
            .insertItem(item)
            .insertSource(source)

        val relation = updatedJson.itemSourceRelation(signature, item, source) ?: return null

        return when {
            item is Item.SoftMatchItem && source is Source.App ->
                updatedJson.copy(itemsIdApps = itemsIdApps.plusElement(relation))
            item is Item.SoftMatchItem && source is Source.Page ->
                updatedJson.copy(itemsIdPages = itemsIdPages.plusElement(relation))
            item is Item.UrlMatchItem && source is Source.App ->
                updatedJson.copy(itemsUrlRootApps = itemsUrlRootApps.plusElement(relation))
            item is Item.UrlMatchItem && source is Source.Page ->
                updatedJson.copy(itemsUrlRootPages = itemsUrlRootPages.plusElement(relation))
            else -> null
        }
    }

    private fun sourceRelation(signature: String, source: Source): List<Int>? {
        val signatureIndex = signatures.indexOfOrNull(signature) ?: return null
        val sourceIndex = indexOfSource(source) ?: return null

        return listOf(signatureIndex, sourceIndex)
    }

    private fun itemSourceRelation(signature: String, item: Item, source: Source): List<Int>? {
        val signatureIndex = signatures.indexOfOrNull(signature) ?: return null
        val itemIndex = indexOfItem(item) ?: return null
        val sourceIndex = indexOfSource(source) ?: return null

        return listOf(signatureIndex, itemIndex, sourceIndex)
    }

    private fun indexOfSignature(signature: String): Int? = signatures.indexOfOrNull(signature)

    private fun indexOfItem(item: Item): Int? {
        return when (item) {
            is Item.SoftMatchItem -> itemsIds.indexOfOrNull(item.value)
            is Item.UrlMatchItem -> itemsUrlRoot.indexOfOrNull(item.value)
        }
    }

    private fun indexOfSource(source: Source): Int? {
        return when (source) {
            is Source.App -> apps.indexOfOrNull(source.packageName)
            is Source.Page -> pages.indexOfOrNull(listOf(source.packageName, source.url))
        }
    }

    private fun insertSignature(signatureFingerprint: String): SecurityWarningsJson {
        return indexOfSignature(signatureFingerprint)?.let { this }
            ?: copy(signatures = signatures.plusElement(signatureFingerprint))
    }

    private fun insertItem(item: Item): SecurityWarningsJson {
        return indexOfItem(item)?.let { this } ?: when (item) {
            is Item.SoftMatchItem -> copy(itemsIds = itemsIds.plusElement(item.value))
            is Item.UrlMatchItem -> copy(itemsUrlRoot = itemsUrlRoot.plusElement(item.value))
        }
    }

    private fun insertSource(source: Source): SecurityWarningsJson {
        return indexOfSource(source)?.let { this } ?: when (source) {
            is Source.App -> copy(apps = apps.plusElement(source.packageName))
            is Source.Page -> copy(pages = pages.plusElement(listOf(source.packageName, source.url)))
        }
    }

    private fun <T> List<T>.indexOfOrNull(element: T): Int? {
        return this.indexOf(element).takeIf {
            it != -1
        }
    }
}
