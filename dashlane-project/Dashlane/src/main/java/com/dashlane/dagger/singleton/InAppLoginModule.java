package com.dashlane.dagger.singleton;

import android.content.Context;

import com.dashlane.inapplogin.InAppLoginByAccessibilityManager;
import com.dashlane.inapplogin.InAppLoginByAutoFillApiManager;
import com.dashlane.inapplogin.InAppLoginManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Module
public class InAppLoginModule {

    @Provides
    @Singleton
    public InAppLoginManager provideInAppLoginManager(@ApplicationContext Context context) {
        return new InAppLoginManager(new InAppLoginByAccessibilityManager(context),
                                     InAppLoginByAutoFillApiManager.createIfPossible(context));
    }
}
