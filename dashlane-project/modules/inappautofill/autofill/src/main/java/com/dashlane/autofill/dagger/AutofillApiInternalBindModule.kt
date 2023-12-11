package com.dashlane.autofill.dagger

import com.dashlane.autofill.FillRequestHandler
import com.dashlane.autofill.FillRequestHandlerImpl
import com.dashlane.autofill.SaveRequestHandler
import com.dashlane.autofill.SaveRequestHandlerImpl
import com.dashlane.autofill.fillresponse.DataSetCreator
import com.dashlane.autofill.fillresponse.DataSetCreatorImpl
import com.dashlane.autofill.fillresponse.FillResponseCreator
import com.dashlane.autofill.fillresponse.FillResponseCreatorImpl
import com.dashlane.autofill.fillresponse.RemoteViewProvider
import com.dashlane.autofill.fillresponse.RemoteViewProviderImpl
import com.dashlane.autofill.request.autofill.database.ItemLoader
import com.dashlane.autofill.request.autofill.database.ItemLoaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
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
