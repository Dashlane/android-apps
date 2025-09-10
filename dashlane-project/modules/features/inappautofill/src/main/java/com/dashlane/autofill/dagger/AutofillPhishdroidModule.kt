package com.dashlane.autofill.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
class AutofillPhishdroidModule {
    @Provides
    @PhishdroidFile
    fun providePhishdroidDirectory(
        @ApplicationContext context: Context,
    ): File =
        File(context.filesDir, PHISHING_FILES_FOLDER_NAME)

    companion object {
        private const val PHISHING_FILES_FOLDER_NAME = "phishdroid"
    }
}

@MustBeDocumented
@Qualifier
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class PhishdroidFile