package com.dashlane.item.passwordhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.item.passwordhistory.PasswordHistoryEntry.CredentialPasswordHistoryEntry
import com.dashlane.item.passwordhistory.PasswordHistoryEntry.GeneratedPasswordHistoryEntry
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GeneratedPasswordQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.date.RelativeDateFormatter
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.history.DataChangeHistoryField
import com.dashlane.vault.history.password
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.isNotSemanticallyNull
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class PasswordHistoryViewModel @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val dataChangeHistoryQuery: DataChangeHistoryQuery,
    private val generatedPasswordQuery: GeneratedPasswordQuery,
    private val relativeDateFormatter: RelativeDateFormatter,
    private val dataSaver: DataSaver,
    private val vaultItemLogger: VaultItemLogger,
) : ViewModel() {
    private val _historyState = MutableStateFlow<PasswordHistoryState>(PasswordHistoryState.Init)
    val historyState = _historyState.asStateFlow()

    fun reloadForGeneratedPasswords() {
        viewModelScope.launch {
            val generatedPasswords = generatedPasswordQuery
                .queryAllNotRevoked()
                .sortedByDescending { it.syncObject.generatedDate }
            val passwordHistory = generatedPasswords.mapNotNull { generatedPassword ->
                buildGeneratedPasswordHistoryOrNull(
                    password = generatedPassword.syncObject.password?.toString(),
                    instant = generatedPassword.syncObject.generatedDate?.let { Instant.ofEpochSecond(it) },
                    domain = generatedPassword.syncObject.domain
                        ?.toUrlDomainOrNull()
                        ?.value,
                    saved = generatedPassword.syncObject.authId.isNotSemanticallyNull(),
                )
            }
            if (passwordHistory.isEmpty()) {
                _historyState.emit(PasswordHistoryState.Error)
            } else {
                _historyState.emit(PasswordHistoryState.Loaded(entries = passwordHistory))
            }
        }
    }

    fun reloadForItemUid(uid: String) {
        viewModelScope.launch {
            
            val currentPassword = getLoginForUid(uid)?.syncObject?.password?.toString()
            if (currentPassword == null) {
                _historyState.emit(PasswordHistoryState.Error)
                return@launch
            }

            
            val filter = DataChangeHistoryFilter(SyncObjectType.AUTHENTIFIANT, uid)
            val passwordHistory = dataChangeHistoryQuery
                .query(filter)
                ?.syncObject
                ?.extractPasswordHistory(uid)

            if (passwordHistory.isNullOrEmpty()) {
                _historyState.emit(PasswordHistoryState.Error)
            } else {
                _historyState.emit(PasswordHistoryState.Loaded(entries = passwordHistory))
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "kotlin:S6531")
    private suspend fun getLoginForUid(uid: String): VaultItem<SyncObject.Authentifiant>? =
        vaultDataQuery.query(
            VaultFilter(
                dataType = SyncObjectType.AUTHENTIFIANT,
                uid = uid,
            ),
        ) as VaultItem<SyncObject.Authentifiant>?

    private fun SyncObject.DataChangeHistory.extractPasswordHistory(itemUid: String): List<PasswordHistoryEntry>? {
        val passwordChangeSets = changeSets
            ?.filter { it.changedProperties?.contains(DataChangeHistoryField.PASSWORD.field) ?: false }
            ?.sortedByDescending { it.modificationDate } ?: return null

        return passwordChangeSets
            .mapIndexed { index, item ->
                
                buildPasswordHistoryOrNull(
                    uid = itemUid,
                    password = item.password,
                    instant = passwordChangeSets[max(0, index - 1)].modificationDate,
                )
            }.filterNotNull()
    }

    private fun buildPasswordHistoryOrNull(uid: String, password: String?, instant: Instant?) =
        if (password == null || instant == null) {
            null
        } else {
            CredentialPasswordHistoryEntry(
                credentialId = uid,
                password = password,
                lastModifiedDateString = relativeDateFormatter.format(instant),
            )
        }

    private fun buildGeneratedPasswordHistoryOrNull(password: String?, instant: Instant?, domain: String?, saved: Boolean) =
        if (password == null || instant == null) {
            null
        } else {
            GeneratedPasswordHistoryEntry(
                password = password,
                lastModifiedDateString = relativeDateFormatter.format(instant),
                domain = domain,
                saved = saved,
            )
        }

    fun restorePassword(selectedPasswordHistory: CredentialPasswordHistoryEntry) {
        viewModelScope.launch {
            val item = getLoginForUid(selectedPasswordHistory.credentialId)

            if (item == null) {
                _historyState.emit(PasswordHistoryState.Error)
                return@launch
            }

            dataSaver.save(item.copySyncObject { password = SyncObfuscatedValue(selectedPasswordHistory.password) })
            vaultItemLogger.logPasswordRestored(itemId = item.uid, url = item.urlForGoToWebsite)

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