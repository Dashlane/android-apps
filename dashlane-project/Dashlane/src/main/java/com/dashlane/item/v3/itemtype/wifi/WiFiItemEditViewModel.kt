package com.dashlane.item.v3.itemtype.wifi

import android.annotation.SuppressLint
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.dashlane.events.AppEvents
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.v3.itemtype.wifi.builder.WiFiFormBuilder
import com.dashlane.item.v3.itemtype.wifi.builder.WiFiHiddenFieldsBuilder
import com.dashlane.item.v3.itemtype.wifi.builder.WiFiQRCodeContentBuilder
import com.dashlane.item.v3.itemtype.wifi.builder.WiFiSuggestionBuilder
import com.dashlane.item.v3.itemtype.wifi.loader.WiFiAsyncLoader
import com.dashlane.item.v3.itemtype.wifi.loader.WiFiSensitiveFieldLoader
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.buildInfoBoxToDisplay
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.ItemEditSideEffect
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.permission.PermissionsManager
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createWifi
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("LargeClass")
@HiltViewModel
class WiFiItemEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dataSync: DataSync,
    genericDataQuery: GenericDataQuery,
    appEvents: AppEvents,
    menuActionHelper: MenuActionHelper,
    formBuilder: WiFiFormBuilder,
    vaultItemCopy: VaultItemCopyService,
    frozenStateManager: FrozenStateManager,
    itemEditRepository: ItemEditRepository,
    sharingPolicyDataProvider: SharingPolicyDataProvider,
    navigator: Navigator,
    vaultDataQuery: VaultDataQuery,
    wiFiAsyncLoader: WiFiAsyncLoader,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    private val wiFiHiddenFieldsBuilder: WiFiHiddenFieldsBuilder,
    private val wiFiSensitiveFieldLoader: WiFiSensitiveFieldLoader,
    private val wiFiQRCodeContentBuilder: WiFiQRCodeContentBuilder,
    private val wiFiSuggestionBuilder: WiFiSuggestionBuilder,
    private val wifiManager: WifiManager,
    private val locationManager: LocationManager,
    private val permissionsManager: PermissionsManager,
    private val vaultItemLogger: VaultItemLogger,
) : ItemEditViewModel<WiFiFormData>(
        savedStateHandle = savedStateHandle,
        dataSync = dataSync,
        genericDataQuery = genericDataQuery,
        vaultDataQuery = vaultDataQuery,
        vaultItemCopy = vaultItemCopy,
        itemEditRepository = itemEditRepository,
        appEvents = appEvents,
        menuActionHelper = menuActionHelper,
        formDataBuilder = formBuilder,
        asyncDataLoader = wiFiAsyncLoader,
        frozenStateManager = frozenStateManager,
        sharingPolicyDataProvider = sharingPolicyDataProvider,
        navigator = navigator,
    ) {
    override fun createNewItem(): VaultItem<SyncObject.WiFi> = createWifi()

    override fun hasError(current: Data<WiFiFormData>): Boolean =
        current.formData.ssid.isBlank()

    override suspend fun ItemEditState<WiFiFormData>.loadSensitiveField(field: SensitiveField): ItemEditState<WiFiFormData> {
        return when (field) {
            SensitiveField.WIFI_PASSPHRASE -> {
                val passphrase = wiFiSensitiveFieldLoader.loadPassphrase(this)
                copyCurrentFormData {
                    it.copy(passphrase = passphrase)
                }.copyInitialFormData { initial, _ ->
                    if (initial.passphrase == null) {
                        initial.copy(passphrase = passphrase)
                    } else {
                        initial
                    }
                }
            }

            else -> this
        }
    }

    @Suppress(
        
        "kotlin:S6313",
    )
    override suspend fun goToEditMode(state: ItemEditState<WiFiFormData>): ItemEditState<WiFiFormData> {
        return super.goToEditMode(state)
            .copy(hiddenFields = emptySet())
            .loadSensitiveField(SensitiveField.WIFI_PASSPHRASE)
            .buildInfoBoxToDisplay()
            .copyCurrentFormData {
                it.copy(
                    suggestedNetworks = loadSSidNetworks(),
                )
            }
    }

    override fun goToViewMode(state: ItemEditState<WiFiFormData>): ItemEditState<WiFiFormData> {
        return super.goToViewMode(state)
            .copy(
                hiddenFields = state.datas?.current
                    ?.let { wiFiHiddenFieldsBuilder(data = it.formData) }
                    ?: state.hiddenFields,
            )
            .buildInfoBoxToDisplay()
    }

    override suspend fun onAllDataLoaded() {
        super.onAllDataLoaded()

        mutableUiState.update {
            it.buildInfoBoxToDisplay(
                isFrozenState = frozenStateManager.isAccountFrozen,
                passwordLimitCount = frozenStateManager.passwordLimitCount,
                locationEnabled = locationManager.isLocationEnabled,
                locationPermissionEnabled = permissionsManager.isAllowed(android.Manifest.permission.ACCESS_FINE_LOCATION),
                isWiFiEnabled = wifiManager.isWifiEnabled,
            )
        }
    }

    fun handleAction(action: WiFiItemEditAction) {
        when (action) {
            is WiFiItemEditAction.SsidChanged -> onSsidChanged(action.ssid)
            is WiFiItemEditAction.PassphraseChanged -> onPassphraseChanged(action.passphrase)
            is WiFiItemEditAction.RevealHidePassphrase -> onRevealHidePassphrase()
            is WiFiItemEditAction.NameChanged -> onNameChanged(action.name)
            is WiFiItemEditAction.NoteChanged -> onNoteChanged(action.note)
            is WiFiItemEditAction.CopySsid -> onCopySsid()
            is WiFiItemEditAction.CopyPassphrase -> onCopyPassphrase()
            is WiFiItemEditAction.CopyNote -> onCopyNote()
            is WiFiItemEditAction.SpaceChanged -> onSpaceChanged(action.space)
            is WiFiItemEditAction.Delete -> onDelete()
            is WiFiItemEditAction.ShowQRCodeBottomSheet -> onShowQRCode()
            is WiFiItemEditAction.HideQRCodeBottomSheet -> onHideQRCode()
            is WiFiItemEditAction.RefreshWifiInfoState -> onRefreshWifiInfoState()
            is WiFiItemEditAction.ShowLocationPermissionDialog -> onShowLocationPermissionDialog()
            is WiFiItemEditAction.ConnectToNetwork -> onConnectToNetwork()
            is WiFiItemEditAction.ConnectToNetworkResult -> onConnectToNetworkResult(action.result)
        }
    }

    private fun onSsidChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    it.copy(
                        ssid = value,
                    )
                }
            }
        }
    }

    private fun onPassphraseChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    val value = value.takeIf { it.isNotBlank() }
                    it.copy(
                        passphrase = value,
                        isPassphraseEmpty = value == null,
                    )
                }
            }
        }
    }

    private fun onRevealHidePassphrase() {
        viewModelScope.launch(defaultDispatcher) {
            val state = mutableUiState.value
            if (state.revealedFields.contains(SensitiveField.WIFI_PASSPHRASE)) {
                hideSensitiveField(SensitiveField.WIFI_PASSPHRASE)
            } else {
                showSensitiveField(SensitiveField.WIFI_PASSPHRASE)
            }
        }
    }

    private fun onNameChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                state.copyCurrentCommonData {
                    it.copy(name = value.takeIf { it.isNotBlank() })
                }
            }
        }
    }

    private fun onNoteChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    it.copy(note = value)
                }
            }
        }
    }

    private fun onCopySsid() {
        copyToClipboard(CopyField.WiFiSsid)
    }

    private fun onCopyPassphrase() {
        copyToClipboard(CopyField.WiFiPassphrase)
    }

    private fun onCopyNote() {
        copyToClipboard(CopyField.WiFiNote)
    }

    private fun onSpaceChanged(space: TeamSpace) {
        viewModelScope.launch(defaultDispatcher) {
            updateSpace(space)
        }
    }

    private fun onShowQRCode() {
        viewModelScope.launch(defaultDispatcher) {
            val itemId = mutableUiState.value.itemId ?: return@launch

            vaultItemLogger.logDisplayWiFiQrCode(itemId)
            mutableUiState
                .update {
                    it.loadSensitiveField(SensitiveField.WIFI_PASSPHRASE)
                        .copyCurrentFormData {
                            it.copy(
                                isQRCodeBottomSheetVisible = true,
                                qrCodeContent = wiFiQRCodeContentBuilder(
                                    ssid = it.ssid,
                                    passphrase = it.passphrase,
                                ),
                            )
                        }
                }
        }
    }

    private fun onHideQRCode() {
        mutableUiState.update {
            it.copyCurrentFormData {
                it.copy(
                    isQRCodeBottomSheetVisible = false,
                )
            }
        }
    }

    private fun onRefreshWifiInfoState() {
        mutableUiState.update { state ->
            state
                .buildInfoBoxToDisplay()
                .copyCurrentFormData {
                    it.copy(
                        suggestedNetworks = loadSSidNetworks(),
                    )
                }
        }
    }

    private fun onShowLocationPermissionDialog() {
        mutableUiState.update {
            it.copy(
                dialog = WiFiDialog.LocationPermissionDenied,
            )
        }
    }

    private fun onConnectToNetwork() {
        viewModelScope.launch {
            val itemSSID = mutableUiState.value.datas?.current?.formData?.ssid ?: return@launch

            @Suppress("DEPRECATION")
            val connectedSSID: String? = wifiManager.connectionInfo.ssid.removeSurrounding("\"")

            if (itemSSID == connectedSSID) {
                mutableUiState.send(
                    ItemEditSideEffect.ConnectToWiFiNetworkResult.AlreadyConnected,
                )

                return@launch
            }

            mutableUiState.update {
                it.loadSensitiveField(SensitiveField.WIFI_PASSPHRASE)
            }
            val itemId = mutableUiState.value.itemId ?: return@launch
            val formData = mutableUiState.value.datas?.current?.formData ?: return@launch
            vaultItemLogger.logConnectToWiFi(itemId)

            mutableUiState.send(
                ItemEditSideEffect.ConnectToWiFiNetwork(
                    network = wiFiSuggestionBuilder.buildSuggestion(
                        ssid = formData.ssid,
                        passphrase = formData.passphrase,
                    ),
                ),
            )
        }
    }

    private fun onConnectToNetworkResult(result: ConnectToWiFiNetworkResultCode) {
        viewModelScope.launch {
            when (result) {
                ConnectToWiFiNetworkResultCode.SUCCESS -> {
                    val itemSSID = mutableUiState.value
                        .datas
                        ?.current
                        ?.formData
                        ?.ssid ?: return@launch

                    @Suppress("DEPRECATION")
                    val connectedSSID: String? = wifiManager.connectionInfo
                        .ssid
                        .removeSurrounding("\"")

                    if (itemSSID == connectedSSID) {
                        mutableUiState.update {
                            it.copyCurrentFormData {
                                it.copy(
                                    isAlreadyConnectedInfoBoxVisible = false,
                                    isConnectToNetworkButtonVisible = false,
                                )
                            }
                        }
                        mutableUiState.send(
                            ItemEditSideEffect.ConnectToWiFiNetworkResult.Connected,
                        )
                    } else {
                        mutableUiState.send(
                            ItemEditSideEffect.ConnectToWiFiNetworkResult.Added,
                        )
                    }
                }
                ConnectToWiFiNetworkResultCode.ALREADY_EXISTS ->
                    mutableUiState.send(
                        ItemEditSideEffect.ConnectToWiFiNetworkResult.AlreadyExists,
                    )
                ConnectToWiFiNetworkResultCode.FAILED ->
                    mutableUiState.send(
                        ItemEditSideEffect.ConnectToWiFiNetworkResult.Failed,
                    )
                else -> {
                    
                }
            }
        }
    }

    private fun onDelete() {
        super.actionDelete()
    }

    private fun ItemEditState<WiFiFormData>.buildInfoBoxToDisplay(): ItemEditState<WiFiFormData> =
        buildInfoBoxToDisplay(
            isFrozenState = frozenStateManager.isAccountFrozen,
            passwordLimitCount = frozenStateManager.passwordLimitCount,
            locationEnabled = locationManager.isLocationEnabled,
            locationPermissionEnabled = permissionsManager.isAllowed(android.Manifest.permission.ACCESS_FINE_LOCATION),
            isWiFiEnabled = wifiManager.isWifiEnabled,
        )

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun loadSSidNetworks(): List<String> {
        return if (wifiManager.isWifiEnabled) {
            wifiManager.scanResults
                .sortedByDescending { it.level }
                .mapNotNull {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.wifiSsid?.toString()
                    } else {
                        it.SSID
                    }
                }
                .map { it.removeSurrounding("\"") }
                .filter { it.isNotBlank() }
                .distinct()
        } else {
            emptyList()
        }
    }
}

sealed class WiFiItemEditAction {
    data class SsidChanged(
        val ssid: String,
    ) : WiFiItemEditAction()

    data object CopySsid : WiFiItemEditAction()

    data class PassphraseChanged(val passphrase: String) : WiFiItemEditAction()

    data class NameChanged(val name: String) : WiFiItemEditAction()

    data class NoteChanged(val note: String) : WiFiItemEditAction()

    data object ShowQRCodeBottomSheet : WiFiItemEditAction()

    data object HideQRCodeBottomSheet : WiFiItemEditAction()

    data object ConnectToNetwork : WiFiItemEditAction()

    data class ConnectToNetworkResult(
        val result: ConnectToWiFiNetworkResultCode,
    ) : WiFiItemEditAction()

    data object CopyPassphrase : WiFiItemEditAction()

    data object CopyNote : WiFiItemEditAction()

    data object RevealHidePassphrase : WiFiItemEditAction()

    data class SpaceChanged(
        val space: TeamSpace,
    ) : WiFiItemEditAction()

    data object Delete : WiFiItemEditAction()

    data object RefreshWifiInfoState : WiFiItemEditAction()

    data object ShowLocationPermissionDialog : WiFiItemEditAction()
}