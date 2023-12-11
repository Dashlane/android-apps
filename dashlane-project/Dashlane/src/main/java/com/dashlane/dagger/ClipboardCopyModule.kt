package com.dashlane.dagger

import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.clipboard.ClipboardCopyImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ClipboardCopyModule {
    @Binds
    fun bindClipboardCopy(impl: ClipboardCopyImpl): ClipboardCopy
}