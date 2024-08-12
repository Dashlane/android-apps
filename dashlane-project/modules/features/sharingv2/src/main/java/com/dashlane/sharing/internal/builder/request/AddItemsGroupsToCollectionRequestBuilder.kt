package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.AddItemGroupsToCollectionService
import com.dashlane.server.api.endpoints.sharinguserdevice.AddItemGroupsToCollectionService.Request.ItemGroup.Permission.ADMIN
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.pattern.UuidFormat
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.sharing.exception.RequestBuilderException.AddItemsGroupToCollectionRequestException
import com.dashlane.sharing.util.AuditLogHelper
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddItemsGroupsToCollectionRequestBuilder @Inject constructor(
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val sharingCryptography: SharingCryptographyHelper,
    private val sessionManager: SessionManager,
    private val auditLogHelper: AuditLogHelper
) {

    private val session: Session?
        get() = sessionManager.session

    private val login: String
        get() = session!!.userId

    @Throws(AddItemsGroupToCollectionRequestException::class)
    suspend fun create(
        collection: Collection,
        itemGroups: List<ItemGroup>,
        userGroups: List<UserGroup>
    ) = withContext(defaultCoroutineDispatcher) {
        val groups = itemGroups.map { itemGroup ->
            val itemGroupKey = sharingCryptography.getItemGroupKeyFromUser(itemGroup, login)
                ?: throw AddItemsGroupToCollectionRequestException("Item Group Key can't be found")
            val itemGroupKeyEncrypted =
                sharingCryptography.generateGroupKeyEncrypted(
                    itemGroupKey,
                    collection.publicKey
                ) ?: throw AddItemsGroupToCollectionRequestException(
                    "Item Group Key can't be encrypted"
                )
            val proposeSignature = sharingCryptography.generateProposeSignature(
                collection.uuid,
                itemGroupKey
            )
            val collectionPrivateKey = sharingCryptography.getCollectionPrivateKey(
                collection,
                userGroups,
                login
            ) ?: throw AddItemsGroupToCollectionRequestException(
                "Can't retrieve Collection Private Key"
            )
            val acceptSignature = sharingCryptography.generateAcceptationSignature(
                itemGroup.groupId,
                itemGroupKey.toByteArray(),
                collectionPrivateKey
            ) ?: throw AddItemsGroupToCollectionRequestException(
                "Accept Signature can't be generated"
            )
            val auditLogDetails = auditLogHelper.buildAuditLogDetails(itemGroup = itemGroup)
            AddItemGroupsToCollectionService.Request.ItemGroup(
                uuid = UuidFormat(itemGroup.groupId),
                itemGroupKey = itemGroupKeyEncrypted,
                permission = ADMIN, 
                proposeSignature = proposeSignature,
                acceptSignature = acceptSignature,
                auditLogDetails = auditLogDetails
            )
        }
        AddItemGroupsToCollectionService.Request(
            collectionId = UuidFormat(collection.uuid),
            itemGroups = groups,
            revision = collection.revision
        )
    }
}