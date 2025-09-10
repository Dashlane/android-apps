package com.dashlane.sharingcenter

import com.dashlane.mvvm.State
import com.dashlane.sharing.SharingContact
import com.dashlane.udf.Event
import com.dashlane.vault.item.VaultListItemState

data class SharingCenterPeopleData(
    val users: List<SharingContact.User> = emptyList(),
    val groups: List<SharingContact.UserGroup> = emptyList(),
)

data class SharingCenterPendingRequestData(
    val itemInvites: List<Pair<SharingContact.ItemInvite, VaultListItemState>> = emptyList(),
    val groupInvites: List<SharingContact.UserGroupInvite> = emptyList(),
    val collectionInvites: List<SharingContact.CollectionInvite> = emptyList(),
)

data class SharingCenterContentData(
    val collections: List<VaultListItemState> = emptyList(),
    val items: List<SharedItemData> = emptyList(),
)

data class SharedItemData(
    val itemType: String,
    val vaultListItemState: VaultListItemState,
)

sealed class SharingCenterState : State {
    sealed class View : SharingCenterState(), State.View {
        abstract val peopleData: SharingCenterPeopleData
        abstract val pendingRequestData: SharingCenterPendingRequestData
        abstract val contentData: SharingCenterContentData

        data class Initial(
            override val peopleData: SharingCenterPeopleData,
            override val pendingRequestData: SharingCenterPendingRequestData,
            override val contentData: SharingCenterContentData,
        ) : View()

        data class Loading(
            override val peopleData: SharingCenterPeopleData,
            override val pendingRequestData: SharingCenterPendingRequestData,
            override val contentData: SharingCenterContentData,
        ) : View()

        data class Display(
            override val peopleData: SharingCenterPeopleData,
            override val pendingRequestData: SharingCenterPendingRequestData,
            override val contentData: SharingCenterContentData,
        ) : View()

        sealed class ProcessPendingInviteResponse : View() {
            data class ProcessPendingCollectionInviteAccept(
                override val peopleData: SharingCenterPeopleData,
                override val pendingRequestData: SharingCenterPendingRequestData,
                override val contentData: SharingCenterContentData,
                val collectionInvite: SharingContact.CollectionInvite,
            ) : ProcessPendingInviteResponse()

            data class ProcessPendingCollectionInviteDecline(
                override val peopleData: SharingCenterPeopleData,
                override val pendingRequestData: SharingCenterPendingRequestData,
                override val contentData: SharingCenterContentData,
                val collectionInvite: SharingContact.CollectionInvite,
            ) : ProcessPendingInviteResponse()

            data class ProcessPendingGroupInviteAccept(
                override val peopleData: SharingCenterPeopleData,
                override val pendingRequestData: SharingCenterPendingRequestData,
                override val contentData: SharingCenterContentData,
                val groupInvite: SharingContact.UserGroupInvite,
            ) : ProcessPendingInviteResponse()

            data class ProcessPendingGroupInviteDecline(
                override val peopleData: SharingCenterPeopleData,
                override val pendingRequestData: SharingCenterPendingRequestData,
                override val contentData: SharingCenterContentData,
                val groupInvite: SharingContact.UserGroupInvite,
            ) : ProcessPendingInviteResponse()

            data class ProcessPendingItemInviteAccept(
                override val peopleData: SharingCenterPeopleData,
                override val pendingRequestData: SharingCenterPendingRequestData,
                override val contentData: SharingCenterContentData,
                val itemInvite: SharingContact.ItemInvite,
                val title: String,
            ) : ProcessPendingInviteResponse()

            data class ProcessPendingItemInviteDecline(
                override val peopleData: SharingCenterPeopleData,
                override val pendingRequestData: SharingCenterPendingRequestData,
                override val contentData: SharingCenterContentData,
                val itemInvite: SharingContact.ItemInvite,
                val title: String,
            ) : ProcessPendingInviteResponse()
        }
    }

    sealed class SideEffect : SharingCenterState(), State.SideEffect {
        data class ShowToast(val message: String) : SideEffect()
    }
}

sealed class SharingCenterEvent : Event {
    data class Loaded(
        val peopleData: SharingCenterPeopleData,
        val pendingRequestData: SharingCenterPendingRequestData,
        val contentData: SharingCenterContentData,
    ) : SharingCenterEvent()

    data object ViewStarted : SharingCenterEvent()

    sealed class PendingRequestEvent : SharingCenterEvent() {
        data class AcceptItemInviteEvent(val invite: SharingContact.ItemInvite, val title: String) :
            PendingRequestEvent()

        data class DeclineItemInviteEvent(val invite: SharingContact.ItemInvite, val title: String) :
            PendingRequestEvent()

        data class AcceptGroupInviteEvent(val invite: SharingContact.UserGroupInvite) : PendingRequestEvent()

        data class DeclineGroupInviteEvent(val invite: SharingContact.UserGroupInvite) : PendingRequestEvent()

        data class AcceptCollectionInviteEvent(val invite: SharingContact.CollectionInvite) : PendingRequestEvent()

        data class DeclineCollectionInviteEvent(val invite: SharingContact.CollectionInvite) : PendingRequestEvent()
    }

    data object PendingRequestResponseFailure : SharingCenterEvent()

    data class PendingRequestResponseSuccess(val message: String) : SharingCenterEvent()

    data object FrozenAccountEvent : SharingCenterEvent()
}