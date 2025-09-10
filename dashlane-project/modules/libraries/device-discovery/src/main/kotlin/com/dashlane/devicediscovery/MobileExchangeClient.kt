package com.dashlane.devicediscovery

import android.content.Context
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MobileExchangeClient @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val capabilityClient: CapabilityClient,
    private val nodeClient: NodeClient,
) {
    suspend fun listPairedDevices(): List<PairedDevice> {
        
        val allNodes = getConnectedNodes().toList()
        
        val dashlaneNodeIds = getNodesWithMobileApp().map {
            it.id
        }

        return allNodes.map {
            PairedDevice(
                id = it.id,
                name = it.displayName,
                isNearby = it.isNearby,
                hasDashlane = it.id in dashlaneNodeIds,
            )
        }
    }

    private suspend fun getNodesWithMobileApp(): List<Node> = withContext(ioDispatcher) {
        Tasks.await(
            capabilityClient.getCapability(
                context.getString(R.string.dashlane_mobile_capability),
                CapabilityClient.FILTER_REACHABLE,
            ),
        ).nodes.toList()
    }

    private suspend fun getConnectedNodes(): MutableList<Node> = withContext(ioDispatcher) {
        Tasks.await(
            nodeClient.connectedNodes,
        )
    }
}