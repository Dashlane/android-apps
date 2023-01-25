package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.api.FillRequestHandler
import com.dashlane.autofill.api.FillRequestHandlerImpl
import com.dashlane.autofill.api.SaveRequestHandler
import com.dashlane.autofill.api.SaveRequestHandlerImpl
import com.dashlane.autofill.api.fillresponse.DataSetCreator
import com.dashlane.autofill.api.fillresponse.DataSetCreatorImpl
import com.dashlane.autofill.api.fillresponse.FillResponseCreator
import com.dashlane.autofill.api.fillresponse.FillResponseCreatorImpl
import com.dashlane.autofill.api.fillresponse.RemoteViewProvider
import com.dashlane.autofill.api.fillresponse.RemoteViewProviderImpl
import com.dashlane.autofill.api.request.autofill.database.ItemLoader
import com.dashlane.autofill.api.request.autofill.database.ItemLoaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
internal interface AutofillApiInternalBindModule {
    @Binds
    fun bindsFillRequestHandler(impl: FillRequestHandlerImpl): FillRequestHandler

    @Binds
    fun bindsSaveRequestHandler(impl: SaveRequestHandlerImpl): SaveRequestHandler

    @Binds
    fun bindsItemLoader(impl: ItemLoaderImpl): ItemLoader

    @Binds
    fun bindsFillResponseCreator(impl: FillResponseCreatorImpl): FillResponseCreator

    @Binds
    fun bindsRemoteViewProvider(impl: RemoteViewProviderImpl): RemoteViewProvider

    @Binds
    fun bindsDataSetCreator(impl: DataSetCreatorImpl): DataSetCreator
}
