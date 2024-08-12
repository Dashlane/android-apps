package com.dashlane.dagger

import com.dashlane.util.date.RelativeDateFormatter
import com.dashlane.util.date.RelativeDateFormatterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface DateModule {

    @Binds
    fun bindsRelativeDateFormatter(impl: RelativeDateFormatterImpl): RelativeDateFormatter
}