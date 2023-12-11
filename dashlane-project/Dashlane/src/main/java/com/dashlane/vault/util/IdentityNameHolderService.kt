package com.dashlane.vault.util

import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

interface IdentityNameHolderService {
    fun getOwner(item: SummaryObject.DriverLicence): String
    fun getOwner(item: SummaryObject.IdCard): String
    fun getOwner(item: SummaryObject.Passport): String
    fun getOwner(item: SummaryObject.SocialSecurityStatement): String
    fun getOwner(item: SummaryObject.FiscalStatement): String
}

class IdentityNameHolderServiceImpl @Inject constructor(private val dataAccessor: MainDataAccessor) :
    IdentityNameHolderService {
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

    override fun getOwner(item: SummaryObject.DriverLicence) = getIdentityFullName(item.linkedIdentity, item.fullname)

    override fun getOwner(item: SummaryObject.IdCard) = getIdentityFullName(item.linkedIdentity, item.fullname)

    override fun getOwner(item: SummaryObject.Passport) = getIdentityFullName(item.linkedIdentity, item.fullname)

    override fun getOwner(item: SummaryObject.SocialSecurityStatement) =
        getIdentityFullName(item.linkedIdentity, item.socialSecurityFullname)

    override fun getOwner(item: SummaryObject.FiscalStatement) = getIdentityFullName(item.linkedIdentity, item.fullname)
}