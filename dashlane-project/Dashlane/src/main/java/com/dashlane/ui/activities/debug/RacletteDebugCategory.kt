package com.dashlane.ui.activities.debug

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceGroup
import com.dashlane.cryptography.DecryptionEngine
import com.dashlane.cryptography.asEncryptedFile
import com.dashlane.cryptography.decryptFileToUtf8String
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.session.Session
import com.dashlane.session.repository.RacletteDatabase
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.util.FileUtils
import com.dashlane.util.showToaster
import com.dashlane.vault.summary.groupBySyncObjectType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.ByteString.Companion.encodeUtf8
import org.json.JSONObject
import java.io.File
import kotlin.math.round

internal class RacletteDebugCategory(debugActivity: Activity) : AbstractDebugCategory(debugActivity) {

    private val userDatabaseRepository: UserDatabaseRepository
        get() = SingletonProvider.getComponent().userDatabaseRepository

    private val session: Session
        get() = SingletonProvider.getSessionManager().session!!

    private val racletteDatabase: RacletteDatabase?
        get() = userDatabaseRepository.getRacletteDatabase(session)

    private val isRacletteDatabaseExist: Boolean
        get() = SingletonProvider.getComponent().databaseProvider.exist(session.userId)

    private val decryptionEngine: DecryptionEngine
        get() = SingletonProvider.getComponent().cryptography
            .createDecryptionEngine(session.localKey.cryptographyKey)

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

        val file = File(File(debugActivity.filesDir, "databases"), databaseName)
        val content = File(file, "content")
        val summaries = File(content, "summaries")
        val sharing = File(content, "sharing")
        val summaryFile = File(summaries, "summary.json.aes")
        val syncSummaryFile = File(summaries, "sync_summary.json.aes")
        val dataChangeHistorySummaryFile = File(summaries, "data_change_history_summary.json.aes")
        val sharingItemGroupsFile = File(sharing, "item_groups.json.aes")
        val sharingUserGroupsFile = File(sharing, "user_groups.json.aes")
        val sharingItemContentsFile = File(sharing, "item_contents.json.aes")

        addSizeItems(file, group, summaryFile, syncSummaryFile, dataChangeHistorySummaryFile)

        val decMemory = racletteDatabase.memorySummaryRepository.databaseSummary!!.all
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

        addPreferenceButton(
            group,
            "Sharing UserGroups",
            racletteDatabase.sharingRepository
                .loadUserGroups()
                .size.toString()
        ) { showFile(sharingUserGroupsFile) }

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
                debugActivity,
                "${file.nameWithoutExtension}.html",
                "text/html"
            ) { stream ->
                stream.writer().use { writer -> writer.write(decrypted) }
            }

            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/html")
            }
            runCatching { debugActivity.startActivity(browserIntent) }
                .onFailure {
                    debugActivity.showToaster(
                        "${it::class.java.simpleName} - ${it.message}",
                        Toast.LENGTH_SHORT
                    )
                }
        }
        return true
    }

    private fun filePermissionsMissing(): Boolean {
        val denied =
            !SingletonProvider.getPermissionsManager().isAllowedToWriteToPublicFolder()
        if (denied) {
            debugActivity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
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