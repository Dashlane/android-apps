package com.dashlane.sync.repositories

import androidx.annotation.FloatRange

sealed class SyncProgress {

    data object Start : SyncProgress()

    data object RemoteSync : SyncProgress()

    data class DecipherRemote(val currentIndex: Int, val count: Int) : SyncProgress() {

        @get:FloatRange(from = 0.0, to = 1.0)
        val progress
            get() = currentIndex.toFloat() / count
    }

    data object Upload : SyncProgress()

    data class LocalSync(val currentIndex: Int, val count: Int) : SyncProgress() {

        @get:FloatRange(from = 0.0, to = 1.0)
        val progress
            get() = currentIndex.toFloat() / count
    }

    data object SharingSync : SyncProgress()

    data object TreatProblem : SyncProgress()
}
