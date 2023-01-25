package com.dashlane.autofill.api.dagger

import android.content.Context
import com.dashlane.autofill.api.FillRequestHandler
import com.dashlane.autofill.api.SaveRequestHandler
import com.dashlane.autofill.api.fillresponse.DataSetCreator
import com.dashlane.autofill.api.fillresponse.RemoteViewProvider
import com.dashlane.autofill.api.internal.AutofillApiApplication
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.request.autofill.database.ItemLoader
import dagger.BindsInstance
import dagger.Component
import dagger.hilt.android.qualifiers.ApplicationContext

@Component(
    modules = [
        AutofillApiInternalModule::class,
        AutofillApiInternalBindModule::class
    ],
    dependencies = [
        AutofillApiComponent::class
    ]
)
internal interface AutofillApiInternalComponent {
    val remoteViewProvider: RemoteViewProvider
    val fillRequestHandler: FillRequestHandler
    val saveRequestHandler: SaveRequestHandler
    val dataSetCreator: DataSetCreator
    val itemLoader: ItemLoader

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            @ApplicationContext
            applicationContext: Context,
            autofillApiViewAllAccountsComponent: AutofillApiComponent
        ): AutofillApiInternalComponent
    }

    companion object {
        operator fun invoke(context: Context) =
            DaggerAutofillApiInternalComponent.factory()
                .create(
                    context.applicationContext,
                    (context.applicationContext as AutofillApiApplication).component
                )
    }
}
