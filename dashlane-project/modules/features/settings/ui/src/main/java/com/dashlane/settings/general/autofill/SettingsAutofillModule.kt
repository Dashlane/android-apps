package com.dashlane.settings.general.autofill

import android.content.Context
import arrow.core.partially1
import com.dashlane.autofillapi.ChromeAutofillDetector
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.inappautofill.AutofillPhishingLogger
import com.dashlane.inappautofill.InAppLoginManager
import com.dashlane.lock.LockManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.settings.Autofill
import com.dashlane.settings.SettingsEvent
import com.dashlane.udf.Strategy
import com.dashlane.udf.UseCase
import com.dashlane.udf.toFilteredUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(ViewModelComponent::class)
object SettingsAutofillModule {
    @Provides
    @ViewModelScoped
    @Suppress("kotlin:S107", "LongMethod")
    fun provideSettingsAutofillUseCases(
        @ApplicationContext context: Context,
        sessionManager: SessionManager,
        frozenStateManager: FrozenStateManager,
        inAppLoginManager: InAppLoginManager,
        preferencesManager: PreferencesManager,
        userFeaturesChecker: UserFeaturesChecker,
        autofillPhishingLogger: AutofillPhishingLogger,
        chromeAutofillDetector: ChromeAutofillDetector,
        lockManager: LockManager,
    ): MutableList<UseCase<SettingsAutofillState.View, SettingsEvent<Autofill>>> {
        return mutableListOf(
            ::loadAutofillSettings
                .partially1(context)
                .partially1(sessionManager)
                .partially1(frozenStateManager)
                .partially1(inAppLoginManager)
                .partially1(preferencesManager)
                .partially1(userFeaturesChecker)
                .partially1(chromeAutofillDetector)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::refreshAutofill
                .partially1(context)
                .partially1(inAppLoginManager)
                .partially1(chromeAutofillDetector)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateAutofill
                .partially1(frozenStateManager)
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateChromeAutofill
                .partially1(context)
                .partially1(chromeAutofillDetector)
                .partially1(frozenStateManager)
                .partially1(lockManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updateKeyboardAutofill
                .partially1(sessionManager)
                .partially1(preferencesManager)
                .toFilteredUseCase(Strategy.CancelOnNewState),
            ::updatePhishingAlert
                .partially1(sessionManager)
                .partially1(preferencesManager)
                .partially1(autofillPhishingLogger)
                .toFilteredUseCase(Strategy.CancelOnNewState),
        )
    }
}