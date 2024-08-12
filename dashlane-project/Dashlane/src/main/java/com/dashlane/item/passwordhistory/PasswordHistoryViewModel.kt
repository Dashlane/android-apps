package com.dashlane.item.passwordhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.util.date.RelativeDateFormatter
import com.dashlane.vault.history.DataChangeHistoryField
import com.dashlane.vault.history.password
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PasswordHistoryViewModel @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val dataChangeHistoryQuery: DataChangeHistoryQuery,
    private val relativeDateFormatter: RelativeDateFormatter,
    private val dataSaver: DataSaver,
) : ViewModel() {
    private val _historyState = MutableStateFlow<PasswordHistoryState>(PasswordHistoryState.Init)
    val historyState = _historyState.asStateFlow()

    fun reloadForItemUid(uid: String) {
        viewModelScope.launch {
            
            val currentPassword = getLoginForUid(uid)?.syncObject?.password?.toString()
            if (currentPassword == null) {
                _historyState.emit(PasswordHistoryState.Error)
                return@launch
            }

            
            val filter = DataChangeHistoryFilter(SyncObjectType.AUTHENTIFIANT, uid)
            val passwordHistory = dataChangeHistoryQuery.query(filter)
                ?.syncObject
                ?.extractPasswordHistory()

            if (passwordHistory.isNullOrEmpty()) {
                _historyState.emit(PasswordHistoryState.Error)
            } else {
                _historyState.emit(PasswordHistoryState.Loaded(entries = passwordHistory))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun getLoginForUid(uid: String): VaultItem<SyncObject.Authentifiant>? =
        vaultDataQuery.query(
            VaultFilter(
                dataType = SyncObjectType.AUTHENTIFIANT,
                uid = uid
            )
        ) as? VaultItem<SyncObject.Authentifiant>

    private fun SyncObject.DataChangeHistory.extractPasswordHistory(): List<PasswordHistoryEntry>? {
        val passwordChangeSets = changeSets
            ?.filter { it.changedProperties?.contains(DataChangeHistoryField.PASSWORD.field) ?: false }
            ?.sortedByDescending { it.modificationDate } ?: return null

        return passwordChangeSets.mapIndexed { index, item ->
            
            buildPasswordHistoryOrNull(
                password = item.password,
                instant = passwordChangeSets[max(0, index - 1)].modificationDate
            )
        }.filterNotNull()
    }

    private fun buildPasswordHistoryOrNull(password: String?, instant: Instant?): PasswordHistoryEntry? =
        if (password == null || instant == null) {
            null
        } else {
            PasswordHistoryEntry(password = password, lastModifiedDateString = relativeDateFormatter.format(instant))
        }

    fun restorePassword(vaultItemUid: String, selectedPasswordHistory: PasswordHistoryEntry) {
        viewModelScope.launch {
            val item = getLoginForUid(vaultItemUid)

            if (item == null) {
                _historyState.emit(PasswordHistoryState.Error)
                return@launch
            }

            dataSaver.save(item.copySyncObject { password = SyncObfuscatedValue(selectedPasswordHistory.password) })

            _historyState.emit(PasswordHistoryState.Success)
        }
    }

    sealed class PasswordHistoryState {
        abstract val entries: List<PasswordHistoryEntry>

        data object Init : PasswordHistoryState() {
            override val entries: List<PasswordHistoryEntry>
                get() = listOf()
        }

        data class Loaded(override val entries: List<PasswordHistoryEntry>) : PasswordHistoryState()

        data object Error : PasswordHistoryState() {
            override val entries: List<PasswordHistoryEntry>
                get() = listOf()
        }

        data object Success : PasswordHistoryState() {
            override val entries: List<PasswordHistoryEntry>
                get() = listOf()
        }
    }
}