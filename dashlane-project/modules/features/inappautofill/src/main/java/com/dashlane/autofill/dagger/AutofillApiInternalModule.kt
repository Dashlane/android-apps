package com.dashlane.autofill.dagger

import android.content.Context
import android.os.Build
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.api.util.AutofillValueFactoryAndroidImpl
import com.dashlane.autofill.fillresponse.InlinePresentationProvider
import com.dashlane.autofill.fillresponse.InlinePresentationProviderImpl
import com.dashlane.autofill.util.AutofillNavigationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object AutofillApiInternalModule {

    @Provides
    fun providesAutofillValueFactory(): AutofillValueFactory {
        return AutofillValueFactoryAndroidImpl()
    }

    @Provides
    fun providesInlinePresentationProvider(
        @ApplicationContext context: Context,
        navigationService: AutofillNavigationService
    ): InlinePresentationProvider? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            InlinePresentationProviderImpl(context, navigationService)
        } else {
            null
        }
    }
}
