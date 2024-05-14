package com.dashlane.accountstatus

interface AccountStatusPostUpdateManager {
    suspend fun onUpdate(newStatus: AccountStatus, oldStatus: AccountStatus?)
}
