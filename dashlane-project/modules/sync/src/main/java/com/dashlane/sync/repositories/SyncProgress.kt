package com.dashlane.sync.repositories

import androidx.annotation.FloatRange
import kotlinx.coroutines.channels.SendChannel

sealed class SyncProgress {

    object RemoteSync : SyncProgress()

    data class DecipherRemote(val currentIndex: Int, val count: Int) : SyncProgress() {

        @get:FloatRange(from = 0.0, to = 1.0)
        val progress
            get() = currentIndex.toFloat() / count
    }

    object Upload : SyncProgress()

    data class LocalSync(val currentIndex: Int, val count: Int) : SyncProgress() {

        @get:FloatRange(from = 0.0, to = 1.0)
        val progress
            get() = currentIndex.toFloat() / count
    }

    object SharingSync : SyncProgress()

    object TreatProblem : SyncProgress()
}

typealias SyncProgressChannel = SendChannel<SyncProgress>
