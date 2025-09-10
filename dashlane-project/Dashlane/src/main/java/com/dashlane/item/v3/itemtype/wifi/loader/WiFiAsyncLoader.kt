package com.dashlane.item.v3.itemtype.wifi.loader

import android.net.wifi.WifiManager
import android.os.Build
import com.dashlane.item.v3.ItemEditFragmentArgs
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.loaders.AsyncDataLoader
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KFunction1

class WiFiAsyncLoader @Inject constructor(
    private val wifiManager: WifiManager,
) : AsyncDataLoader<WiFiFormData> {
    private val additionalAsyncData: MutableList<Deferred<Unit>> = mutableListOf()

    override fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        navArgs: ItemEditFragmentArgs,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<WiFiFormData>) -> Data<WiFiFormData>, Unit>,
        onAllDataLoaded: suspend () -> Unit,
    ) {
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                @Suppress("DEPRECATION")
                val connectedSSID: String? =
                    wifiManager.connectionInfo.ssid?.removeSurrounding("\"")
                val isConnected = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    (initialSummaryObject as SummaryObject.WiFi).ssid == connectedSSID

                additionalDataLoadedFunction {
                    it.updateFormData {
                        it.copy(
                            isAlreadyConnectedInfoBoxVisible = isConnected,
                            isConnectToNetworkButtonVisible = !isConnected,
                        )
                    }
                }
            },
        )

        scope.launch {
            additionalAsyncData.joinAll()
            onAllDataLoaded()
        }
    }

    override fun cancelAll() {
        additionalAsyncData.forEach { it.cancel() }
    }
}