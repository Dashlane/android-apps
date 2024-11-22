package com.dashlane.abtesting

import com.dashlane.server.api.Authorization

interface RemoteConfiguration {

    suspend fun initAndRefresh(authorization: Authorization.User) {
        val loadResult = load(authorization.login)
        if (loadResult == LoadResult.Success) {
            launchRefreshIfNeeded(authorization)
        } else {
            refreshIfNeeded(authorization)
        }
    }

    fun load(username: String): LoadResult

    fun launchRefreshIfNeeded(authorization: Authorization.User)

    suspend fun refreshIfNeeded(authorization: Authorization.User)

    enum class LoadResult {
        Success,
        Failure
    }
}