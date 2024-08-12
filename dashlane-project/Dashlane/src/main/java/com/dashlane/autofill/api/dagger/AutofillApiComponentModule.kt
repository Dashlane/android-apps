package com.dashlane.autofill.api.dagger

import com.dashlane.autofill.changepassword.AutoFillChangePasswordConfiguration
import com.dashlane.guidedpasswordchange.OnboardingGuidedPasswordChangeActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AutofillApiComponentModule {

    @Provides
    fun providesAutoFillChangePasswordConfiguration(): AutoFillChangePasswordConfiguration {
        val domainUsername = OnboardingGuidedPasswordChangeActivity.currentDomainUsername

        return if (domainUsername == null) {
            AutoFillChangePasswordConfiguration()
        } else {
            AutoFillChangePasswordConfiguration(
                filterOnDomain = domainUsername.first,
                filterOnUsername = domainUsername.second,
                onItemUpdated = {
                    OnboardingGuidedPasswordChangeActivity.itemUpdated = true
                },
                onDomainChanged = {
                    OnboardingGuidedPasswordChangeActivity.currentDomainUsername = null
                }
            )
        }
    }
}