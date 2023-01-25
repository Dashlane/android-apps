package com.dashlane.vault.util

import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

open class IdentityUtil @Inject constructor(private val dataAccessor: MainDataAccessor) {
    private fun getIdentityFullName(identityUid: String?, fallbackName: String?): String {
        if (identityUid.isNotSemanticallyNull()) {
            val identity = dataAccessor
                .getGenericDataQuery()
                .queryFirst(GenericFilter(identityUid!!, SyncObjectType.IDENTITY)) as? SummaryObject.Identity
            identity
                ?.fullName
                ?.takeIf { it.isNotSemanticallyNull() }
                ?.let { return it }
        }
        return fallbackName.takeIf { it.isNotSemanticallyNull() } ?: ""
    }

    open fun getOwner(item: SummaryObject.DriverLicence) = getIdentityFullName(item.linkedIdentity, item.fullname)

    open fun getOwner(item: SummaryObject.IdCard) = getIdentityFullName(item.linkedIdentity, item.fullname)

    open fun getOwner(item: SummaryObject.Passport) = getIdentityFullName(item.linkedIdentity, item.fullname)

    open fun getOwner(item: SummaryObject.SocialSecurityStatement) =
        getIdentityFullName(item.linkedIdentity, item.socialSecurityFullname)

    open fun getOwner(item: SummaryObject.FiscalStatement) = getIdentityFullName(item.linkedIdentity, item.fullname)
}