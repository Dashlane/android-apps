package com.dashlane.accountstatus

import com.dashlane.user.Username

interface AccountStatusPostUpdateManager {
    suspend fun onUpdate(username: Username, newStatus: AccountStatus, oldStatus: AccountStatus?)
}
