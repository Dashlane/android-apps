package com.dashlane.sharing.model

import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.RsaStatus
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserCollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupCollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember

fun List<ItemGroupRaclette>.toItemGroups(): List<ItemGroup> = map { it.toItemGroup() }

fun ItemGroupRaclette.toItemGroup(): ItemGroup {
    return ItemGroup(
        type = ItemGroup.Type.ITEMS,
        groupId = this.groupId,
        revision = this.revision.toLong(),
        users = this.users?.toUserDownloads(),
        groups = this.groups?.toUserGroupMembers(),
        items = this.items.toItemKeys(),
        collections = this.collections?.toCollectionDownloads()
    )
}

fun List<CollectionDownloadRaclette>.toCollectionDownloads() = map { it.toCollectionDownload() }

fun CollectionDownloadRaclette.toCollectionDownload() = CollectionDownload(
    itemGroupKey = this.itemGroupKey,
    referrer = this.referrer,
    name = this.name,
    permission = this.permission.toPermission(),
    proposeSignature = this.proposeSignature,
    uuid = this.uuid,
    status = this.status.toStatus(),
    acceptSignature = this.acceptSignature
)

fun List<UserDownloadRaclette>.toUserDownloads(): List<UserDownload> = map { it.toUserDownload() }

fun UserDownloadRaclette.toUserDownload(): UserDownload {
    return UserDownload(
        userId = this.userId,
        alias = this.alias,
        permission = this.permission.toPermission(),
        proposeSignature = this.proposeSignature,
        acceptSignature = this.acceptSignature,
        groupKey = this.groupKey,
        status = this.status.toStatus(),
        referrer = this.referrer,
        proposeSignatureUsingAlias = this.proposeSignatureUsingAlias,
        rsaStatus = this.rsaStatus.toRsaStatus()
    )
}

fun List<UserGroupMemberRaclette>.toUserGroupMembers(): List<UserGroupMember> =
    map { it.toUserGroupMember() }

fun UserGroupMemberRaclette.toUserGroupMember(): UserGroupMember {
    return UserGroupMember(
        groupId = this.groupId,
        name = this.name,
        status = this.status.toStatus(),
        permission = this.permission.toPermission(),
        proposeSignature = this.proposeSignature,
        acceptSignature = this.acceptSignature,
        groupKey = this.groupKey,
        referrer = this.referrer
    )
}

fun List<ItemKeyRaclette>.toItemKeys(): List<ItemGroup.Item> = map { it.toItemKey() }

fun ItemKeyRaclette.toItemKey(): ItemGroup.Item {
    return ItemGroup.Item(itemId = this.itemId, itemKey = this.itemKey)
}

fun List<UserGroupRaclette>.toUserGroups(): List<UserGroup> = map { it.toUserGroup() }

fun List<CollectionRaclette>.toCollections(): List<Collection> = map { it.toCollection() }

fun List<Collection>.toCollectionRaclettes(): List<CollectionRaclette> =
    map { it.toCollectionRaclette() }

fun UserGroupRaclette.toUserGroup(): UserGroup {
    return UserGroup(
        type = UserGroup.Type.USERS,
        groupId = this.groupId,
        name = this.name,
        teamId = this.teamId?.toLong(),
        publicKey = this.publicKey,
        privateKey = this.privateKey,
        revision = this.revision.toLong(),
        users = this.users.toUserDownloads(),
    )
}

fun ItemGroup.toItemGroupRaclette(): ItemGroupRaclette {
    return ItemGroupRaclette(
        groupId = this.groupId,
        revision = this.revision.toInt(),
        users = this.users?.toList()?.toUserDownloadRaclettes(),
        groups = this.groups?.toList()?.toUserGroupMemberRaclettes(),
        collections = this.collections?.toList()?.toCollectionDownloadRaclettes(),
        items = this.items?.toItemKeyRaclettes() ?: emptyList()
    )
}

fun List<ItemGroup>.toItemGroupRaclettes(): List<ItemGroupRaclette> =
    map { it.toItemGroupRaclette() }

fun List<UserDownload>.toUserDownloadRaclettes(): List<UserDownloadRaclette> =
    map { it.toUserDownloadRaclette() }

fun UserDownload.toUserDownloadRaclette(): UserDownloadRaclette {
    return UserDownloadRaclette(
        userId = this.userId,
        alias = this.alias,
        permission = this.permission.key,
        proposeSignature = this.proposeSignature,
        acceptSignature = this.acceptSignature,
        groupKey = this.groupKey,
        status = this.status?.key ?: "",
        referrer = this.referrer,
        proposeSignatureUsingAlias = this.proposeSignatureUsingAlias ?: false,
        rsaStatus = this.rsaStatus?.key ?: ""
    )
}

fun List<UserGroupMember>.toUserGroupMemberRaclettes(): List<UserGroupMemberRaclette> =
    map { it.toUserGroupMemberRaclette() }

fun UserGroupMember.toUserGroupMemberRaclette(): UserGroupMemberRaclette {
    return UserGroupMemberRaclette(
        this.groupId,
        this.name,
        this.status.key,
        this.permission.key,
        this.proposeSignature,
        this.acceptSignature,
        this.groupKey,
        this.referrer
    )
}

fun List<ItemGroup.Item>.toItemKeyRaclettes(): List<ItemKeyRaclette> =
    map { it.toItemKeyRaclette() }

fun ItemGroup.Item.toItemKeyRaclette(): ItemKeyRaclette {
    return ItemKeyRaclette(this.itemId, this.itemKey)
}

fun List<UserGroup>.toUserGroupRaclettes(): List<UserGroupRaclette> =
    map { it.toUserGroupRaclette() }

fun UserGroup.toUserGroupRaclette(): UserGroupRaclette {
    return UserGroupRaclette(
        this.groupId,
        this.name,
        this.teamId?.toString(),
        this.publicKey,
        this.privateKey,
        this.revision.toInt(),
        this.users.toList().toUserDownloadRaclettes()
    )
}

fun List<CollectionDownload>.toCollectionDownloadRaclettes(): List<CollectionDownloadRaclette> =
    map { it.toCollectionDownloadRaclette() }

fun CollectionDownload.toCollectionDownloadRaclette(): CollectionDownloadRaclette =
    CollectionDownloadRaclette(
        itemGroupKey = itemGroupKey,
        referrer = referrer,
        name = name,
        permission = permission.key,
        proposeSignature = proposeSignature,
        uuid = uuid,
        status = status.key,
        acceptSignature = acceptSignature
    )

fun Collection.toCollectionRaclette(): CollectionRaclette {
    return CollectionRaclette(
        privateKey = privateKey,
        userGroups = userGroups?.toCollectionUserGroupRaclettes(),
        name = name,
        publicKey = publicKey,
        uuid = uuid,
        users = users?.toCollectionUserRaclettes(),
        revision = revision
    )
}

private fun List<UserCollectionDownload>.toCollectionUserRaclettes(): List<UserCollectionDownloadRaclette> =
    map { it.toCollectionUserRaclette() }

private fun UserCollectionDownload.toCollectionUserRaclette(): UserCollectionDownloadRaclette =
    UserCollectionDownloadRaclette(
        referrer = referrer,
        permission = permission.key,
        proposeSignature = proposeSignature,
        login = login,
        collectionKey = collectionKey,
        rsaStatus = rsaStatus?.key,
        acceptSignature = acceptSignature,
        status = status.key,
        proposeSignatureUsingAlias = proposeSignatureUsingAlias
    )

private fun List<UserGroupCollectionDownload>.toCollectionUserGroupRaclettes(): List<UserGroupCollectionDownloadRaclette> =
    map { it.toCollectionUserGroupRaclette() }

private fun UserGroupCollectionDownload.toCollectionUserGroupRaclette(): UserGroupCollectionDownloadRaclette =
    UserGroupCollectionDownloadRaclette(
        referrer = referrer,
        name = name,
        permission = permission.key,
        proposeSignature = proposeSignature,
        uuid = uuid,
        collectionKey = collectionKey,
        status = status.key,
        acceptSignature = acceptSignature
    )

private fun CollectionRaclette.toCollection(): Collection =
    Collection(
        privateKey = privateKey,
        userGroups = userGroups?.toCollectionUserGroups(),
        name = name,
        publicKey = publicKey,
        uuid = uuid,
        users = users?.toCollectionUsers(),
        revision = revision
    )

private fun List<UserCollectionDownloadRaclette>.toCollectionUsers(): List<UserCollectionDownload> =
    map { it.toCollectionUser() }

private fun UserCollectionDownloadRaclette.toCollectionUser(): UserCollectionDownload =
    UserCollectionDownload(
        referrer = referrer,
        permission = permission.toPermission(),
        proposeSignature = proposeSignature,
        login = login,
        collectionKey = collectionKey,
        rsaStatus = rsaStatus?.toRsaStatus(),
        acceptSignature = acceptSignature,
        status = status.toStatus(),
        proposeSignatureUsingAlias = proposeSignatureUsingAlias
    )

private fun List<UserGroupCollectionDownloadRaclette>.toCollectionUserGroups(): List<UserGroupCollectionDownload> =
    map { it.toCollectionUserGroup() }

private fun UserGroupCollectionDownloadRaclette.toCollectionUserGroup(): UserGroupCollectionDownload =
    UserGroupCollectionDownload(
        referrer = referrer,
        name = name,
        permission = permission.toPermission(),
        proposeSignature = proposeSignature,
        uuid = uuid,
        collectionKey = collectionKey,
        status = status.toStatus(),
        acceptSignature = acceptSignature
    )

fun String.toPermission(): Permission =
    Permission.values().find { it.key == this } ?: Permission.LIMITED

private fun String.toStatus(): Status =
    Status.values().find { it.key == this } ?: Status.REVOKED

private fun String.toRsaStatus(): RsaStatus =
    RsaStatus.values().find { it.key == this } ?: RsaStatus.NOKEY

val Status.isAcceptedOrPending: Boolean
    get() = this == Status.ACCEPTED || this == Status.PENDING

val Status.isAccepted: Boolean
    get() = this == Status.ACCEPTED

val UserDownload.isAcceptedOrPending: Boolean
    get() = isAccepted || isPending
val UserCollectionDownload.isPending: Boolean
    get() = this.status == Status.PENDING
val UserCollectionDownload.isAccepted: Boolean
    get() = this.status == Status.ACCEPTED
val UserCollectionDownload.isAcceptedOrPending: Boolean
    get() = status.isAcceptedOrPending
val UserCollectionDownload.isAdmin: Boolean
    get() = this.permission == Permission.ADMIN
val UserGroupCollectionDownload.isAccepted: Boolean
    get() = this.status == Status.ACCEPTED
val UserGroupCollectionDownload.isAcceptedOrPending: Boolean
    get() = this.status.isAcceptedOrPending
val UserGroupCollectionDownload.isPending: Boolean
    get() = this.status == Status.PENDING
val UserGroupCollectionDownload.isAdmin: Boolean
    get() = this.permission == Permission.ADMIN
val UserDownload.isAdmin: Boolean
    get() = this.permission == Permission.ADMIN
val UserDownload.isLimited: Boolean
    get() = this.permission == Permission.LIMITED
val UserDownload.isAccepted: Boolean
    get() = this.status == Status.ACCEPTED
val UserDownload.isPending: Boolean
    get() = this.status == Status.PENDING

val CollectionDownload.isAccepted: Boolean
    get() = this.status == Status.ACCEPTED
val CollectionDownload.isAcceptedOrPending: Boolean
    get() = this.status.isAcceptedOrPending
val CollectionDownload.isAdmin: Boolean
    get() = this.permission == Permission.ADMIN

val UserGroupMember.isAcceptedOrPending: Boolean
    get() = isAccepted || isPending
val UserGroupMember.isAdmin: Boolean
    get() = this.permission == Permission.ADMIN
val UserGroupMember.isAccepted: Boolean
    get() = this.status == Status.ACCEPTED
val UserGroupMember.isPending: Boolean
    get() = this.status == Status.PENDING

fun getMaxPermission(
    userDownload: UserDownload?,
    userGroupMembers: List<UserGroupMember>,
    collectionDownloads: List<CollectionDownload>
): Permission {
    val list =
        userGroupMembers.map { it.permission } + userDownload?.permission + collectionDownloads.map { it.permission }
    return if (list.contains(Permission.ADMIN)) {
        Permission.ADMIN
    } else {
        Permission.LIMITED
    }
}

fun getMaxStatus(
    userDownload: UserDownload?,
    userGroupMembers: List<UserGroupMember>,
    collectionDownloads: List<CollectionDownload>
): Status {
    val list =
        userGroupMembers.map { it.status } + userDownload?.status + collectionDownloads.map { it.status }
    return if (list.contains(Status.ACCEPTED)) {
        Status.ACCEPTED
    } else if (list.contains(Status.PENDING)) {
        Status.PENDING
    } else if (list.contains(Status.REFUSED)) {
        Status.REFUSED
    } else {
        Status.REVOKED
    }
}

fun UserGroup.getUser(login: String) = users.find { it.userId == login }

fun Collection.getUser(login: String) = users?.find { it.login == login }

fun Collection.getUserGroup(groupId: String) = userGroups?.find { it.uuid == groupId }

fun Collection.isAdmin(login: String, userGroupsAccepted: List<UserGroup>) =
    getUser(login)?.isAdmin == true || userGroups?.any { group ->
        group.isAccepted && group.isAdmin && userGroupsAccepted.any { userGroup ->
            userGroup.groupId == group.uuid
        }
    } ?: false