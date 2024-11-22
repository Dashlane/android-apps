package com.dashlane.login.lock

import android.content.Context
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPrompt
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

suspend fun LockManager.unlockItemIfNeeded(
    context: Context,
    dataQuery: GenericDataQuery,
    uid: String,
    type: String,
): Boolean {
    val dataType = SyncObjectType.forXmlNameOrNull(type) ?: return true
    if (hasRecentUnlock()) return true 
    val item = dataQuery.queryFirst(GenericFilter(uid, dataType)) ?: return true
    if (!needUnlock(item)) return true
    
    val lockEvent = showAndWaitLockActivityForItem(context, item)
    if (lockEvent !is LockEvent.Unlock) return false
    val reason = lockEvent.reason as? LockEvent.Unlock.Reason.OpenItem ?: return false
    return reason.itemUid == uid && reason.xmlObjectName == type
}

suspend fun LockManager.showAndWaitLockActivityForItem(context: Context, item: SummaryObject): LockEvent {
    val itemType = item.syncObjectType
    val itemUid = item.id
    val isSecureNote = item is SummaryObject.SecureNote

    val reason = LockEvent.Unlock.Reason.OpenItem(itemType.xmlObjectName, itemUid)
    return showAndWaitLockActivityForReason(
        context = context,
        reason = reason,
        lockPrompt = LockPrompt.ForItem(isSecureNote = isSecureNote),
    )
}
