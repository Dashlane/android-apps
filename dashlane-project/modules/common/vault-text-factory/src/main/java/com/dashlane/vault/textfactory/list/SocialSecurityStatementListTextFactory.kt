package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import javax.inject.Inject

class SocialSecurityStatementListTextFactory @Inject constructor(
    private val resources: Resources,
    private val identityNameHolderService: IdentityNameHolderService
) : DataIdentifierListTextFactory<SummaryObject.SocialSecurityStatement> {

    override fun getTitle(item: SummaryObject.SocialSecurityStatement): StatusText {
        return StatusText(resources.getString(R.string.social_security))
    }

    override fun getDescription(item: SummaryObject.SocialSecurityStatement, default: StatusText): StatusText {
        val hasNoIdentity = item.linkedIdentity.isSemanticallyNull() &&
            (item.socialSecurityFullname.isSemanticallyNull() || item.dateOfBirth == null)
        if (hasNoIdentity || item.isSocialSecurityNumberEmpty) {
            return default
        }
        return StatusText(identityNameHolderService.getOwner(item))
    }
}