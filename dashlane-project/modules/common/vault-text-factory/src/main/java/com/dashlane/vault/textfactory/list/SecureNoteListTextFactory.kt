package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class SecureNoteListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.SecureNote> {

    override fun getTitle(item: SummaryObject.SecureNote): StatusText {
        val title = item.title
        return StatusText(if (title.isNotSemanticallyNull()) title!! else resources.getString(R.string.securenote))
    }

    override fun getDescription(item: SummaryObject.SecureNote, default: StatusText): StatusText {
        return StatusText(
            if (item.secured == true) {
                resources.getString(R.string.secure_note_is_locked)
            } else {
                item.content?.lineSequence()?.firstOrNull()?.take(n = 80) ?: ""
            }
        )
    }
}