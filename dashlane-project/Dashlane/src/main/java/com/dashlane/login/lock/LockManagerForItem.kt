package com.dashlane.login.lock

import android.content.Context
import com.dashlane.lock.UnlockEvent
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.vault.util.SyncObjectTypeUtils.valueFromDesktopIdIfExist



suspend fun LockManager.unlockItemIfNeeded(
    context: Context,
    dataQuery: GenericDataQuery,
    uid: String,
    type: Int
): Boolean {
    val dataType = valueFromDesktopIdIfExist(type) ?: return true
    if (hasRecentUnlock(uid)) return true 
    val item = dataQuery.queryFirst(GenericFilter(uid, dataType)) ?: return true
    if (!needUnlock(item)) return true
    
    val event = showAndWaitLockActivityForItem(context, item)
    if (event?.isSuccess() != true) return false
    val reason = event.reason as? UnlockEvent.Reason.OpenItem ?: return false
    return reason.itemUid == uid && reason.itemType == type
}