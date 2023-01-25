package com.dashlane.securearchive

import java.io.File



sealed class BackupViewState {
    

    object Idle : BackupViewState()

    

    object Processing : BackupViewState()

    

    data class Success(val count: Int) : BackupViewState()

    

    object InvalidPasswordError : BackupViewState()

    

    data class UnhandledError(val t: Throwable) : BackupViewState()

    

    data class FallbackToFileIntent(val count: Int, val cacheFile: File) : BackupViewState()

    

    object Cancelled : BackupViewState()
}