package com.dashlane.vault.model

import com.dashlane.xml.domain.utils.Country
import java.time.Instant

data class DataIdentifierAttrsMutable constructor(
    val original: CommonDataIdentifierAttrs,
) : CommonDataIdentifierAttrs {
    override var id: Int = original.id

    override var uid: String = original.uid

    override var attachments: String? = original.attachments

    override var formatLang: Country? = original.formatLang

    override var syncState: SyncState = original.syncState

    override var hasDirtySharedField: Boolean = original.hasDirtySharedField

    override var sharingPermission: String? = original.sharingPermission

    override var creationDate: Instant? = original.creationDate

    override var userModificationDate: Instant? = original.userModificationDate

    override var locallyViewedDate: Instant? = original.locallyViewedDate

    override var locallyUsedCount: Long = original.locallyUsedCount

    override var teamSpaceId: String? = original.teamSpaceId

    override var backupDate: Instant? = original.backupDate

    fun setStateModifiedIfNotDeleted() {
        if (!isDeleted) {
            syncState = SyncState.MODIFIED
        }
    }

    private fun toDataIdentifierAttrs(): CommonDataIdentifierAttrs {
        return CommonDataIdentifierAttrsImpl(
            id = id,
            uid = uid,
            attachments = attachments,
            formatLang = formatLang,
            syncState = syncState,
            hasDirtySharedField = hasDirtySharedField,
            sharingPermission = sharingPermission,
            creationDate = creationDate,
            userModificationDate = userModificationDate,
            locallyViewedDate = locallyViewedDate,
            teamSpaceId = teamSpaceId,
            locallyUsedCount = locallyUsedCount,
        )
    }

    companion object {
        fun with(
            original: CommonDataIdentifierAttrs,
            block: DataIdentifierAttrsMutable.() -> Unit,
        ): CommonDataIdentifierAttrs {
            val mutable = DataIdentifierAttrsMutable(original)
            mutable.apply(block)
            return mutable.toDataIdentifierAttrs()
        }
    }
}