package com.dashlane.invites

import android.content.Context
import android.widget.Toast
import com.dashlane.R
import com.dashlane.accountstatus.subscriptioncode.SubscriptionCodeRepository
import com.dashlane.server.api.endpoints.invitation.GetSharingLinkService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.util.IntentFactory.sendShareWithFriendsIntent
import com.dashlane.util.NetworkStateProvider
import com.dashlane.util.Toaster
import com.dashlane.util.isNotSemanticallyNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

object InviteFriendsIntentHelper {
    suspend fun launchInviteFriendsIntent(
        context: Context,
        toaster: Toaster,
        subscriptionCodeRepository: SubscriptionCodeRepository,
        sharingLinkService: GetSharingLinkService,
        ioDispatcher: CoroutineDispatcher,
        networkStateProvider: NetworkStateProvider,
    ) {
        if (networkStateProvider.isOn()) {
            try {
                withContext(ioDispatcher) {
                    val subscriptionCode = subscriptionCodeRepository.get()
                    val response = sharingLinkService.execute(
                        request = GetSharingLinkService.Request(
                            userKey = subscriptionCode,
                        )
                    )

                    val sharingId = response.data.sharingId
                    if (sharingId.isNotSemanticallyNull()) {
                        sendShareWithFriendsIntent(context, toaster, sharingId)
                    }
                }
            } catch (ex: DashlaneApiException) {
                toaster.show(R.string.network_failed_notification, Toast.LENGTH_LONG)
            }
        } else {
            toaster.show(R.string.make_sure_you_have_internet_for_refferalid, Toast.LENGTH_LONG)
        }
    }
}