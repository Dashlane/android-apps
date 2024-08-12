package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.toIdentityFormat
import com.dashlane.vault.model.identityPartialOrFullNameNoLogin
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class IdentityListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.Identity> {

    override fun getTitle(item: SummaryObject.Identity): StatusText {
        val name = item.identityPartialOrFullNameNoLogin
        return StatusText(if (name.isNotSemanticallyNull()) name!! else resources.getString(R.string.identity))
    }

    override fun getDescription(item: SummaryObject.Identity, default: StatusText): StatusText {
        return item.birthDate?.toIdentityFormat(resources)?.let { StatusText(it) } ?: default
    }
}