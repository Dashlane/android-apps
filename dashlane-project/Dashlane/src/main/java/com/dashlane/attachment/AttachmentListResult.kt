package com.dashlane.attachment

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.attachment.ui.AttachmentListActivity
import com.dashlane.attachment.ui.AttachmentListActivity.Companion.EXTRA_HAVE_ATTACHMENTS_CHANGED
import com.dashlane.vault.summary.SummaryObject

class AttachmentListResult : ActivityResultContract<SummaryObject, Pair<Boolean, String?>>() {
    override fun createIntent(context: Context, input: SummaryObject): Intent {
        return Intent(context, AttachmentListActivity::class.java).apply {
            putExtra(AttachmentListActivity.ITEM_ATTACHMENTS, input.attachments)
            putExtra(AttachmentListActivity.ITEM_ID, input.id)
            putExtra(AttachmentListActivity.ITEM_TYPE, input.syncObjectType.xmlObjectName)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, String?> {
        if (resultCode != Activity.RESULT_OK || intent == null) return false to null

        val hasAttachmentChanged = intent.getBooleanExtra(EXTRA_HAVE_ATTACHMENTS_CHANGED, false)
        val attachments = if (hasAttachmentChanged) {
            intent.getStringExtra(AttachmentListActivity.EXTRA_ATTACHMENTS_STRING)
        } else {
            null
        }
        return hasAttachmentChanged to attachments
    }
}