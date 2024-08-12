package com.dashlane.urldomain

import android.content.Context
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CleanupLegacyUrlDomainIconDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    operator fun invoke() {
        coroutineScope.launch(ioDispatcher) {
            
            context.deleteDatabase("domain_icons.db")
        }
    }
}