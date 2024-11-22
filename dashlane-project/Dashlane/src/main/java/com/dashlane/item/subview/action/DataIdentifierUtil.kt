package com.dashlane.item.subview.action

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dashlane.attachment.ui.AttachmentListActivity
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.ui.activities.DashlaneWrapperActivity
import com.dashlane.ui.screens.sharing.SharingNewSharePeopleFragment
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary

fun VaultItem<*>.showAttachments(activity: Activity) {
    this.toSummary<SummaryObject>().showAttachments(activity)
}

fun SummaryObject.showAttachments(activity: Activity) {
    val attachmentListIntent = Intent(activity, AttachmentListActivity::class.java).apply {
        putExtra(AttachmentListActivity.ITEM_ATTACHMENTS, attachments)
        putExtra(AttachmentListActivity.ITEM_ID, id)
        putExtra(
            AttachmentListActivity.ITEM_TYPE,
            this@showAttachments.syncObjectType.xmlObjectName
        )
    }
    activity.startActivityForResult(
        attachmentListIntent,
        AttachmentListActivity.REQUEST_CODE_ATTACHMENT_LIST
    )
}

fun SummaryObject.showSharing(
    requestCode: Int,
    activity: Activity,
    startNewShare: Boolean = false
) {
    val uri = NavigationUriBuilder().apply {
        when (this@showSharing) {
            is SummaryObject.Authentifiant -> {
                host(NavigationHelper.Destination.MainPath.PASSWORDS)
                origin(SharingNewSharePeopleFragment.FROM_ITEM_VIEW)
            }
            is SummaryObject.SecureNote -> {
                host(NavigationHelper.Destination.MainPath.NOTES)
                origin(SharingNewSharePeopleFragment.FROM_ITEM_VIEW)
            }
            else -> {
                
            }
        }
        appendPath(id)
        if (startNewShare) {
            appendPath(NavigationHelper.Destination.SecondaryPath.Items.SHARE)
        } else {
            appendPath(NavigationHelper.Destination.SecondaryPath.Items.SHARE_INFO)
        }
    }.build()
    DashlaneWrapperActivity.startActivityForResult(
        requestCode,
        activity,
        uri,
        Bundle().apply {
            if (!startNewShare) putBoolean(ShareDetailsAction.EXTRA_NOTIFY_UID_CHANGES, true)
        }
    )
}

fun VaultItem<*>.showSharing(requestCode: Int, activity: Activity, startNewShare: Boolean = false) {
    toSummary<SummaryObject>().showSharing(requestCode, activity, startNewShare)
}