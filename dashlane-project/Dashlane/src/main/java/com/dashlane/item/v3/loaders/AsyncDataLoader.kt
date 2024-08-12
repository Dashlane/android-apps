package com.dashlane.item.v3.loaders

import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.loaders.AsyncDataLoader.Loader
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

class AsyncDataLoader @Inject constructor(
    private val credentialAsyncDataLoader: Provider<CredentialAsyncDataLoader>
) {
    fun get(clazz: KClass<out SummaryObject>): Loader? = when (clazz) {
        SummaryObject.Authentifiant::class -> credentialAsyncDataLoader.get()
        else -> null
    }

    interface Loader {
        fun loadAsync(
            initialSummaryObject: SummaryObject,
            isNewItem: Boolean,
            scope: CoroutineScope,
            additionalDataLoadedFunction: KFunction1<(FormData) -> FormData, Unit>,
            onAllDataLoaded: suspend () -> Unit
        )

        fun cancelAll()
    }
}