package com.dashlane.teamspaces.db

import kotlinx.coroutines.runBlocking

fun TeamspaceForceCategorizationManager.executeSyncBlocking() {
    runBlocking {
        executeSync()
    }
}