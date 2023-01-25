package com.dashlane.notification.badge



interface SharingInvitationRepository {

    

    suspend fun hasInvitations(): Boolean
}