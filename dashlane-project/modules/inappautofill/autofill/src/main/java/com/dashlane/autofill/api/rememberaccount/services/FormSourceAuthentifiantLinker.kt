package com.dashlane.autofill.api.rememberaccount.services

interface FormSourceAuthentifiantLinker {
    suspend fun isLinked(formSourceIdentifier: String, authentifiantId: String): Boolean
    suspend fun link(formSourceIdentifier: String, authentifiantId: String): Boolean
    suspend fun unlink(formSourceIdentifier: String, authentifiantId: String) {
        
    }

    suspend fun unlinkAll() {
        
    }

    suspend fun allLinked(): List<Pair<String, String>> = emptyList()
}
