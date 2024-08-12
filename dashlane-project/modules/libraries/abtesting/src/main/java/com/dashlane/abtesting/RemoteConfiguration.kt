package com.dashlane.abtesting

interface RemoteConfiguration {

    suspend fun initAndRefresh() {
        val loadResult = load()
        if (loadResult == LoadResult.Success) {
            launchRefreshIfNeeded()
        } else {
            refreshIfNeeded()
        }
    }

    fun load(): LoadResult

    fun launchRefreshIfNeeded()

    suspend fun refreshIfNeeded()

    enum class LoadResult {
        Success,
        Failure
    }
}