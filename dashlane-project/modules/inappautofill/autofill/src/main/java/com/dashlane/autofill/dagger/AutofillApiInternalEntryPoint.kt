package com.dashlane.autofill.dagger

import android.content.Context
import com.dashlane.autofill.FillRequestHandler
import com.dashlane.autofill.SaveRequestHandler
import com.dashlane.autofill.fillresponse.DataSetCreator
import com.dashlane.autofill.request.autofill.database.ItemLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AutofillApiInternalEntryPoint {
    val fillRequestHandler: FillRequestHandler
    val saveRequestHandler: SaveRequestHandler
    val dataSetCreator: DataSetCreator
    val itemLoader: ItemLoader

    companion object {
        operator fun invoke(context: Context) = EntryPointAccessors.fromApplication(
            context,
            AutofillApiInternalEntryPoint::class.java
        )
    }
}
