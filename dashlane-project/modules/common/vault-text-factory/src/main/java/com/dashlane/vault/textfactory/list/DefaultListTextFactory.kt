package com.dashlane.vault.textfactory.list

import android.content.Context
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultListTextFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : DataIdentifierListTextFactory<SummaryObject> {
    override fun getTitle(item: SummaryObject): StatusText {
        if (DeveloperUtilities.systemIsInDebug(context)) {
            return StatusText(
                "TODO: ListTextFactory missing for ${item.javaClass.simpleName} in DataIdentifierListTextResolver",
                true
            )
        }
        return StatusText("", false)
    }

    override fun getDescription(item: SummaryObject, default: StatusText): StatusText {
        if (DeveloperUtilities.systemIsInDebug(context)) {
            return StatusText(
                "TODO: ListTextFactory missing for ${item.javaClass.simpleName} in DataIdentifierListTextResolver",
                true
            )
        }
        return StatusText("", false)
    }
}