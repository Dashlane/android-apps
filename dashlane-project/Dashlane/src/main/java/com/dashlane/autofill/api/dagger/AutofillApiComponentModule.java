package com.dashlane.autofill.api.dagger;

import com.dashlane.autofill.changepassword.AutoFillChangePasswordConfiguration;
import com.dashlane.guidedpasswordchange.OnboardingGuidedPasswordChangeActivity;

import dagger.Module;
import dagger.Provides;
import kotlin.Pair;

@Module
public class AutofillApiComponentModule {

    @Provides
    AutoFillChangePasswordConfiguration providesAutoFillChangePasswordConfiguration() {
        Pair<String, String> domainUsername =
                OnboardingGuidedPasswordChangeActivity.Companion.getCurrentDomainUsername();
        if (domainUsername == null) {
            return new AutoFillChangePasswordConfiguration();
        }
        return new AutoFillChangePasswordConfiguration(
                domainUsername.getFirst(),
                domainUsername.getSecond(),
                () -> {
                    
                    OnboardingGuidedPasswordChangeActivity.Companion.setItemUpdated(true);
                    return null;
                },
                () -> {
                    
                    OnboardingGuidedPasswordChangeActivity.Companion.setCurrentDomainUsername(null);
                    return null;
                }
        );
    }
}
