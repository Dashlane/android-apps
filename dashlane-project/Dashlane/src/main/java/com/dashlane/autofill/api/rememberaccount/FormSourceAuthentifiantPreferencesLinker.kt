package com.dashlane.autofill.api.rememberaccount

import com.dashlane.autofill.api.rememberaccount.services.FormSourceAuthentifiantLinker
import com.dashlane.preference.GlobalPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class FormSourceAuthentifiantPreferencesLinker constructor(
    private val preferencesKey: String,
    val globalPreferencesManager: GlobalPreferencesManager,
    val coroutineContext: CoroutineContext = Dispatchers.IO
) : FormSourceAuthentifiantLinker {

    private val applicationFormSources =
        globalPreferencesManager.getStringSet(preferencesKey)?.toMap() ?: mutableListOf()

    override suspend fun isLinked(formSourceIdentifier: String, authentifiantId: String): Boolean {
        return withContext(coroutineContext) {
            isFormSourceLinked(applicationFormSources, formSourceIdentifier, authentifiantId)
        }
    }

    override suspend fun link(formSourceIdentifier: String, authentifiantId: String): Boolean {
        withContext(coroutineContext) {
            addLinkToPreferences(preferencesKey, formSourceIdentifier, applicationFormSources, authentifiantId)
        }
        return true
    }

    override suspend fun unlink(formSourceIdentifier: String, authentifiantId: String) {
        withContext(coroutineContext) {
            unlinkFromPreferences(preferencesKey, formSourceIdentifier, applicationFormSources, authentifiantId)
        }
    }

    override suspend fun unlinkAll() {
        withContext(coroutineContext) {
            globalPreferencesManager.remove(preferencesKey)
            applicationFormSources.clear()
        }
    }

    override suspend fun allLinked(): List<Pair<String, String>> {
        return withContext(coroutineContext) {
            applicationFormSources
        }
    }

    private fun isFormSourceLinked(
        linkedFormSources: List<Pair<String, String>>,
        formSourceId: String,
        authentifiantId: String
    ): Boolean {
        val linkedFormSource = formSourceId to authentifiantId
        return linkedFormSources.contains(linkedFormSource)
    }

    private fun addLinkToPreferences(
        key: String,
        formSourceId: String,
        formSources: MutableList<Pair<String, String>>,
        authentifiantId: String
    ) {
        val linkToAdd = formSourceId to authentifiantId
        if (!formSources.contains(linkToAdd)) {
            formSources.add(linkToAdd)
        }
        globalPreferencesManager.remove(key)
        globalPreferencesManager.putStringSet(key, formSources.fromMap())
    }

    private fun unlinkFromPreferences(
        key: String,
        formSourceId: String,
        formSources: MutableList<Pair<String, String>>,
        authentifiantId: String
    ) {
        val linkToAdd = formSourceId to authentifiantId
        formSources.remove(linkToAdd)
        globalPreferencesManager.remove(key)
        globalPreferencesManager.putStringSet(key, formSources.fromMap())
    }

    private fun Set<String>.toMap(): MutableList<Pair<String, String>> {
        return this.map {
            it.toEntry()
        }.toMutableList()
    }

    private fun String.toEntry(): Pair<String, String> {
        val linkedFromSource = this.split("#")
        return linkedFromSource[0] to linkedFromSource[1]
    }

    private fun List<Pair<String, String>>.fromMap(): Set<String> {
        return this.map {
            it.fromEntry()
        }.toSet()
    }

    private fun Pair<String, String>.fromEntry(): String {
        return "${this.first}#${this.second}"
    }
}
