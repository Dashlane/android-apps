package com.dashlane.invites

import android.content.Context
import android.widget.Toast
import com.dashlane.R
import com.dashlane.network.webservices.GetSharingLinkService
import com.dashlane.session.SessionManager
import com.dashlane.util.IntentFactory.sendShareWithFriendsIntent
import com.dashlane.util.NetworkStateProvider
import com.dashlane.util.Toaster
import com.dashlane.util.isNotSemanticallyNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object InviteFriendsIntentHelper {
    fun launchInviteFriendsIntent(
        context: Context,
        toaster: Toaster,
        sharingLinkService: GetSharingLinkService,
        sessionManager: SessionManager,
        networkStateProvider: NetworkStateProvider,
        referralId: String?
    ) {
        if (referralId != null) {
            sendShareWithFriendsIntent(context, referralId)
            return
        }
        
        val session = sessionManager.session
        if (session != null && networkStateProvider.isOn()) {
            sharingLinkService.createCall(session.userId, session.uki).enqueue(object :
                Callback<GetSharingLinkService.Content> {
                override fun onResponse(
                    call: Call<GetSharingLinkService.Content>,
                    response: Response<GetSharingLinkService.Content>
                ) {
                    if (response.isSuccessful) {
                        val sharingId = response.body()!!.sharingId
                        if (sharingId.isNotSemanticallyNull()) {
                            sendShareWithFriendsIntent(context, sharingId)
                            return
                        }
                    }
                    
                    toaster.show(R.string.network_failed_notification, Toast.LENGTH_LONG)
                }

                override fun onFailure(call: Call<GetSharingLinkService.Content>, t: Throwable) {
                    toaster.show(R.string.network_failed_notification, Toast.LENGTH_LONG)
                }
            })
        } else {
            toaster.show(R.string.make_sure_you_have_internet_for_refferalid, Toast.LENGTH_LONG)
        }
    }
}