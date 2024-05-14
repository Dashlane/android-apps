package com.dashlane.login.pages.password

import com.dashlane.sync.DataSync
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialSync @Inject constructor(
    private val dataSync: DataSync,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    fun CoroutineScope.launchInitialSync() = launch(defaultDispatcher) {
        launch { dataSync.initialSync() }
    }
}
