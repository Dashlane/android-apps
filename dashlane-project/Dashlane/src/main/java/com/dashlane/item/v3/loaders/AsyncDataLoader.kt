package com.dashlane.item.v3.loaders

import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KFunction1

interface AsyncDataLoader<T : FormData> {
    fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<T>) -> Data<T>, Unit>,
        onAllDataLoaded: suspend () -> Unit
    )

    fun cancelAll()

    fun Data<T>.updateFormData(block: (T) -> T) =
        this.copy(formData = block(this.formData))

    fun Data<T>.updateCommonData(block: (CommonData) -> CommonData) =
        this.copy(commonData = block(this.commonData))
}