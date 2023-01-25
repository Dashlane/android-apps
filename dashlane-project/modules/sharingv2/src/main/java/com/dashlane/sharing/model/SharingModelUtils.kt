package com.dashlane.sharing.model

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.RsaStatus
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember

fun List<ItemGroupRaclette>.toItemGroups(): List<ItemGroup> = map { it.toItemGroup() }

fun ItemGroupRaclette.toItemGroup(): ItemGroup {
    return ItemGroup(
        type = ItemGroup.Type.ITEMS,
        groupId = this.groupId,
        revision = this.revision.toLong(),
        users = this.users?.toUserDownloads(),
        groups = this.groups?.toUserGroupMembers(),
        items = this.items.toItemKeys()
    )
}

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

fun String.toPermission(): Permission =
    Permission.values().find { it.key == this } ?: Permission.LIMITED

private fun String.toStatus(): Status =
    Status.values().find { it.key == this } ?: Status.REVOKED

private fun String.toRsaStatus(): RsaStatus =
    RsaStatus.values().find { it.key == this } ?: RsaStatus.NOKEY

fun Status.isAcceptedOrPending(): Boolean = this == Status.ACCEPTED || this == Status.PENDING
val UserDownload.isAcceptedOrPending: Boolean
    get() = isAccepted || isPending
val UserDownload.isAdmin: Boolean
    get() = this.permission == Permission.ADMIN
val UserDownload.isLimited: Boolean
    get() = this.permission == Permission.LIMITED
val UserDownload.isAccepted: Boolean
    get() = this.status == Status.ACCEPTED
val UserDownload.isPending: Boolean
    get() = this.status == Status.PENDING

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
    userGroupMembers: List<UserGroupMember>
): Permission {
    val list = (userGroupMembers.map { it.permission }) + userDownload?.permission
    return if (list.contains(Permission.ADMIN)) Permission.ADMIN
    else Permission.LIMITED
}

fun getMaxStatus(
    userDownload: UserDownload?,
    userGroupMembers: List<UserGroupMember>
): Status {
    val list = (userGroupMembers.map { it.status }) + userDownload?.status
    return if (list.contains(Status.ACCEPTED)) Status.ACCEPTED
    else if (list.contains(Status.PENDING)) Status.PENDING
    else if (list.contains(Status.REFUSED)) Status.REFUSED
    else Status.REVOKED
}

fun UserGroup.getUser(login: String) = users.find { it.userId == login }

fun UserGroup.getUserStatus(login: String) = getUser(login)?.status