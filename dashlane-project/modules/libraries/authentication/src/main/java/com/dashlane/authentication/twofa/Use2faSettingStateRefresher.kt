package com.dashlane.authentication.twofa

interface Use2faSettingStateRefresher {
    suspend fun refresh()
}