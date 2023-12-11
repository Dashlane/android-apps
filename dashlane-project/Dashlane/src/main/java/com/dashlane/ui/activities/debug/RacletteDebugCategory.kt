package com.dashlane.ui.activities.debug

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceGroup
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.asEncryptedFile
import com.dashlane.cryptography.decryptFileToUtf8String
import com.dashlane.database.DatabaseProvider
import com.dashlane.permission.PermissionsManager
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.RacletteDatabase
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.util.FileUtils
import com.dashlane.util.showToaster
import com.dashlane.vault.summary.groupBySyncObjectType
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.ByteString.Companion.encodeUtf8
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import kotlin.math.round

internal class RacletteDebugCategory @Inject constructor(
    @ActivityContext override val context: Context,
    val userDatabaseRepository: UserDatabaseRepository,
    val cryptography: Cryptography,
    val databaseProvider: DatabaseProvider,
    val sessionManager: SessionManager,
    val permissionsManager: PermissionsManager
) : AbstractDebugCategory() {

    private val session: Session
        get() = sessionManager.session!!

    private val racletteDatabase: RacletteDatabase?
        get() = userDatabaseRepository.getRacletteDatabase(session)

    private val isRacletteDatabaseExist: Boolean
        get() = databaseProvider.exist(session.userId)

    private val decryptionEngine: DecryptionEngine
        get() = cryptography.createDecryptionEngine(session.localKey.cryptographyKey)

    override fun addSubItems(group: PreferenceGroup) {
        addPreferenceCheckbox(
            group,
            "isRacletteDatabaseExist",
            null,
            isRacletteDatabaseExist,
            false,
            null
        )

        val racletteDatabase = racletteDatabase ?: return
        addRacletteItems(group, racletteDatabase)
    }

    override val name: String
        get() = "Raclette"

    private fun addRacletteItems(group: PreferenceGroup, racletteDatabase: RacletteDatabase) {
        val databaseName = session.userId.encodeUtf8().sha256().hex() + ".aes"

        val file = File(File(context.filesDir, "databases"), databaseName)
        val content = File(file, "content")
        val summaries = File(content, "summaries")
        val sharing = File(content, "sharing")
        val summaryFile = File(summaries, "summary.json.aes")
        val syncSummaryFile = File(summaries, "sync_summary.json.aes")
        val dataChangeHistorySummaryFile = File(summaries, "data_change_history_summary.json.aes")
        val sharingItemGroupsFile = File(sharing, "item_groups.json.aes")
        val sharingUserGroupsFile = File(sharing, "user_groups.json.aes")
        val sharingItemContentsFile = File(sharing, "item_contents.json.aes")
        val sharingCollectionsFile = File(sharing, "collection.json.aes")

        addSizeItems(file, group, summaryFile, syncSummaryFile, dataChangeHistorySummaryFile)

        val decMemory = racletteDatabase.memorySummaryRepository.databaseSummary!!.data
            .groupBySyncObjectType().toSortedMap().map {
                "${it.key.xmlObjectName} ${it.value.size}"
            }.joinToString("\n")
        addPreferenceButton(group, "Summary", decMemory) { showFile(summaryFile) }

        val decSyncMemory = racletteDatabase.memorySummaryRepository.databaseSyncSummary!!.items
            .groupBySyncObjectType().toSortedMap().map {
                "${it.key.xmlObjectName} ${it.value.size}"
            }.joinToString("\n")
        addPreferenceButton(group, "SyncSummary", decSyncMemory) { showFile(syncSummaryFile) }

        addPreferenceButton(
            group,
            "DataChangeHistorySummary",
            racletteDatabase.memorySummaryRepository
                .databaseDataChangeHistorySummary?.data?.size.toString()
        ) { showFile(dataChangeHistorySummaryFile) }

        addPreferenceButton(
            group,
            "Sharing ItemGroups",
            racletteDatabase.sharingRepository
                .loadItemGroups()
                .size.toString()
        ) { showFile(sharingItemGroupsFile) }

        val userGroups = racletteDatabase.sharingRepository
            .loadUserGroups()
        addPreferenceButton(
            group,
            "Sharing UserGroups",
            "${userGroups.size}\n${userGroups.joinToString("\n") { it.name }}"
        ) { showFile(sharingUserGroupsFile) }

        val collections = racletteDatabase.sharingRepository
            .loadCollections()
        addPreferenceButton(
            group,
            "Sharing Collection",
            "${collections.size}\n${collections.joinToString("\n") { it.name }}"
        ) { showFile(sharingCollectionsFile) }

        addPreferenceButton(
            group,
            "Sharing ItemContent",
            racletteDatabase.sharingRepository
                .loadItemContents()
                .size.toString()
        ) { showFile(sharingItemContentsFile) }
    }

    private fun addSizeItems(
        file: File,
        group: PreferenceGroup,
        summaryFile: File,
        syncSummaryFile: File,
        dataChangeHistorySummaryFile: File
    ) {
        val sizeKB = file.walkTopDown().map { it.length() }.sum().toSize()
        addPreferenceButton(group, "Total Size", sizeKB, null)

        addPreferenceButton(
            group,
            "summaryFile size",
            summaryFile.length().toSize()
        ) { showFile(summaryFile) }
        addPreferenceButton(
            group,
            "syncSummaryFile size",
            syncSummaryFile.length().toSize()
        ) { showFile(syncSummaryFile) }
        addPreferenceButton(
            group,
            "dataChangeHistorySummaryFile size",
            dataChangeHistorySummaryFile.length().toSize()
        ) { showFile(dataChangeHistorySummaryFile) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showFile(file: File): Boolean {
        if (filePermissionsMissing()) {
            return false
        }
        GlobalScope.launch(Dispatchers.Main) {
            val decrypted = decryptionEngine.decryptFileToUtf8String(
                file.asEncryptedFile(),
                compressed = true
            ).let { JSONObject(it).toString(2) }

            val uri = FileUtils.writeFileToPublicFolder(
                context,
                "${file.nameWithoutExtension}.html",
                "text/html"
            ) { stream ->
                stream.writer().use { writer -> writer.write(decrypted) }
            }

            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/html")
            }
            runCatching { context.startActivity(browserIntent) }
                .onFailure {
                    context.showToaster(
                        "${it::class.java.simpleName} - ${it.message}",
                        Toast.LENGTH_SHORT
                    )
                }
        }
        return true
    }

    private fun filePermissionsMissing(): Boolean {
        context as Activity
        val denied =
            !permissionsManager.isAllowedToWriteToPublicFolder()
        if (denied) {
            context.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
        return denied
    }

    private fun Long.toSize() = (round(this / 1024.0 * 100) / 100).let {
        if (it > 1024) {
            "${round(it / 1024 * 100) / 100} MB"
        } else {
            "$it KB"
        }
    }
}