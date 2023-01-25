package com.dashlane.createaccount.pages.tos

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.channels.Channel

data class AgreedTosEvent(val optInOffers: Boolean) {
    class ChannelHolder : androidx.lifecycle.ViewModel() {
        val channel = Channel<AgreedTosEvent>()

        companion object {
            fun of(activity: FragmentActivity) = ViewModelProvider(activity).get(ChannelHolder::class.java)
        }
    }
}
