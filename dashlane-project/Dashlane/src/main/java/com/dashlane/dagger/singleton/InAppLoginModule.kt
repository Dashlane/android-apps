package com.dashlane.dagger.singleton

import android.content.Context
import com.dashlane.inapplogin.InAppLoginByAccessibilityManager
import com.dashlane.inapplogin.InAppLoginByAutoFillApiManager.Companion.createIfPossible
import com.dashlane.inapplogin.InAppLoginManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InAppLoginModule {
    @Provides
    @Singleton
    fun provideInAppLoginManager(@ApplicationContext context: Context) = InAppLoginManager(
        inAppLoginByAccessibilityManager = InAppLoginByAccessibilityManager(context),
        inAppLoginByAutoFillApiManager = createIfPossible(context)
    )
}