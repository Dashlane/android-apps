package com.dashlane.wear.login.paywall

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.dashlane.devicediscovery.MobileExchangeClient
import com.dashlane.devicediscovery.PairedDevice
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.wear.login.paywall.FreeUserPaywallState.SideEffect
import com.dashlane.wear.login.paywall.FreeUserPaywallState.View
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FreeUserPaywallViewModel @Inject constructor(
    private val mobileExchangeClient: MobileExchangeClient,
    private val remoteActivityHelper: RemoteActivityHelper,
    private val navigationUriBuilder: NavigationUriBuilder,
) : ViewModel() {
    private val _stateFlow = MutableViewStateFlow<View, SideEffect>(View.Default)
    val stateFlow: ViewStateFlow<View, SideEffect> = _stateFlow

    fun onViewStarted() {
        viewModelScope.launch {
            val pairedDevice =
                mobileExchangeClient.listPairedDevices().firstOrNull { it.isNearby }
            _stateFlow.update { View.Paywall(devicesState = pairedDevice) }
        }
    }

    fun onUpgrade(pairedDevice: PairedDevice) {
        viewModelScope.launch {
            val getPremiumUri = if (pairedDevice.hasDashlane) {
                navigationUriBuilder
                    .host(NavigationHelper.Destination.MainPath.GET_PREMIUM)
                    .build()
            } else {
                Uri.parse(WEBSITE_PRICING_PAGE)
            }
            remoteActivityHelper.startRemoteActivity(
                Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(getPremiumUri),
                null,
            )
            _stateFlow.update { View.CheckYourPhone }
            
            delay(5_000)
            onCancel()
        }
    }

    fun onCancel() {
        viewModelScope.launch {
            _stateFlow.send(SideEffect.GoBack)
        }
    }

    companion object {
        const val WEBSITE_PRICING_PAGE = "https://www.dashlane.com/pricing"
    }
}

sealed class FreeUserPaywallState : State {
    sealed class View : FreeUserPaywallState(), State.View {
        abstract val transitionKey: String

        data object Default : View() {
            override val transitionKey: String = "paywall"
        }

        data class Paywall(val devicesState: PairedDevice? = null) : View() {
            override val transitionKey: String = "paywall"
        }

        data object CheckYourPhone : View() {
            override val transitionKey: String = "checkYourPhone"
        }
    }

    sealed class SideEffect : FreeUserPaywallState(), State.SideEffect {
        data object GoBack : SideEffect()
    }
}