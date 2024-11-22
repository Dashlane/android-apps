package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class SecretListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.Secret> {

    override fun getTitle(item: SummaryObject.Secret): StatusText {
        return StatusText(item.title ?: "")
    }

    override fun getDescription(item: SummaryObject.Secret, default: StatusText): StatusText =
        when {
            item.secured == true -> StatusText(resources.getString(R.string.secret_is_locked))
            else -> StatusText("")
        }
}