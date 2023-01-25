package com.dashlane.autofill.api.dagger

import android.content.Context
import android.os.Build
import androidx.annotation.Nullable
import com.dashlane.autofill.api.fillresponse.InlinePresentationProvider
import com.dashlane.autofill.api.fillresponse.InlinePresentationProviderImpl
import com.dashlane.autofill.api.util.AutofillNavigationService
import com.dashlane.autofill.api.util.AutofillValueFactory
import com.dashlane.autofill.api.util.AutofillValueFactoryAndroidImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class)
internal class AutofillApiInternalModule {

    @Provides
    fun providesAutofillValueFactory(): AutofillValueFactory {
        return AutofillValueFactoryAndroidImpl()
    }

    @Nullable
    @Provides
    fun providesInlinePresentationProvider(
        @ApplicationContext context: Context,
        navigationService:
        AutofillNavigationService
    ):
            InlinePresentationProvider? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            InlinePresentationProviderImpl(context, navigationService)
        } else {
            null
        }
    }
}
