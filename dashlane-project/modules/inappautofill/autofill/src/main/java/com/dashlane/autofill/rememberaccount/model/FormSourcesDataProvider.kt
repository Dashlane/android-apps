package com.dashlane.autofill.rememberaccount.model

import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface FormSourcesDataProvider {
    suspend fun link(formSource: AutoFillFormSource, authentifiantId: String)
    suspend fun unlink(formSource: AutoFillFormSource, authentifiantId: String)
    suspend fun isLinked(formSource: AutoFillFormSource, authentifiantId: String): Boolean
    suspend fun getAllLinkedFormSourceAuthentifiantIds(autofillFormSource: AutoFillFormSource): List<String>
}