package com.dashlane.wear.hilt

import android.app.Application
import androidx.fragment.app.Fragment
import com.dashlane.crashreport.CrashReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class WearCrashReporterModule {
    @Provides
    fun providesCrashReporterStub() = object : CrashReporter {
        override val crashReporterId: String
            get() = ""

        override fun init(application: Application) = Unit

        override fun logNonFatal(throwable: Throwable) = Unit

        override fun addInformation(rawTrace: String) = Unit

        override fun addInformation(traceWithDate: String, rawTrace: String) = Unit

        override fun addLifecycleInformation(fragment: Fragment?, cycle: String) = Unit
    }
}