package com.dashlane.database

import com.dashlane.database.DatabaseFiles.Companion.AES_JSON_FILE_EXTENSION
import com.dashlane.database.DatabaseFiles.Companion.BACKUPS_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.CONTENT_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.DATA_CHANGE_HISTORY_SUMMARIES_FILE
import com.dashlane.database.DatabaseFiles.Companion.INDEX_FILE
import com.dashlane.database.DatabaseFiles.Companion.ITEMS_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.ITEM_CONTENTS_FILE
import com.dashlane.database.DatabaseFiles.Companion.ITEM_GROUPS_FILE
import com.dashlane.database.DatabaseFiles.Companion.SEARCH_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.SEARCH_QUERY_FILE
import com.dashlane.database.DatabaseFiles.Companion.SHARING_COLLECTIONS_FILE
import com.dashlane.database.DatabaseFiles.Companion.SHARING_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.SUMMARIES_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.SUMMARIES_FILE
import com.dashlane.database.DatabaseFiles.Companion.SYNC_SUMMARIES_FILE
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_DELETE_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_MARKER
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_SEARCH_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_SEARCH_MARKER
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_SHARING_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_SHARING_MARKER
import com.dashlane.database.DatabaseFiles.Companion.TRANSACTION_UPDATE_DIRECTORY
import com.dashlane.database.DatabaseFiles.Companion.USER_GROUPS_FILE
import java.io.File

internal interface DatabaseFiles {
    val content: Content

    val transactionRoot: File

    val transactionContent: Content

    val transactionDelete: TransactionDelete

    val marker: Marker

    val index: File

    val transactionSharingContent: Content

    val transactionSearchContent: Content

    fun delete(): Boolean

    fun rename(newDatabaseName: String): Boolean

    companion object {
        const val DATABASE_EXTENSION = ".aes"
        const val JSON_FILE_EXTENSION = "json"
        const val AES_JSON_FILE_EXTENSION = "$JSON_FILE_EXTENSION.aes"
        const val CONTENT_DIRECTORY = "content"
        const val SUMMARIES_DIRECTORY = "summaries"
        const val ITEMS_DIRECTORY = "items"
        const val SHARING_DIRECTORY = "sharing"
        const val BACKUPS_DIRECTORY = "backups"
        const val SUMMARIES_FILE = "summary"
        const val SYNC_SUMMARIES_FILE = "sync_summary"
        const val DATA_CHANGE_HISTORY_SUMMARIES_FILE = "data_change_history_summary"
        const val TRANSACTION_DIRECTORY = "transaction"
        const val TRANSACTION_UPDATE_DIRECTORY = "update"
        const val TRANSACTION_DELETE_DIRECTORY = "delete"
        const val TRANSACTION_MARKER = "transaction_x"
        const val TRANSACTION_SHARING_MARKER = "transaction_x_sharing"
        const val TRANSACTION_SEARCH_MARKER = "transaction_x_search"
        const val INDEX_FILE = "index"
        const val ITEM_GROUPS_FILE = "item_groups"
        const val USER_GROUPS_FILE = "user_groups"
        const val ITEM_CONTENTS_FILE = "item_contents"
        const val SHARING_COLLECTIONS_FILE = "sharing_collections"
        const val SEARCH_DIRECTORY = "search"
        const val SEARCH_QUERY_FILE = "search_query"
        const val TRANSACTION_SHARING_DIRECTORY = "transaction_sharing"
        const val TRANSACTION_SEARCH_DIRECTORY = "transaction_search"
    }
}

internal interface Content {
    val fileExtension: String

    val rootDir: File

    operator fun get(id: Id): File

    fun getItemFile(fileName: String): File

    val itemsDirectory: File

    val summaryDirectory: File

    val sharingDirectory: File

    val summaryFile: File

    val syncSummaryFile: File

    val dataChangeHistorySummaryFile: File

    val sharingItemGroupsFile: File

    val sharingUserGroupsFile: File

    val sharingItemContentsFile: File

    val sharingCollectionsFile: File

    val backupDirectory: File

    val searchDirectory: File

    val searchQueryFile: File

    fun getBackupFile(id: Id): File

    fun getBackupFile(fileName: String): File

    fun delete() = rootDir.deleteRecursively()
}

internal interface TransactionDelete {
    val deleteDirectory: File

    fun getFileToDelete(id: Id): File
}

private class ContentImpl(
    override val rootDir: File,
    override val fileExtension: String = AES_JSON_FILE_EXTENSION,
) :
    Content {
    override val itemsDirectory: File
        get() = File(rootDir, ITEMS_DIRECTORY).also { it.mkdirs() }

    override val sharingDirectory: File
        get() = File(rootDir, SHARING_DIRECTORY).also { it.mkdirs() }

    override val summaryDirectory: File
        get() = File(rootDir, SUMMARIES_DIRECTORY).also { it.mkdirs() }

    override val searchDirectory: File
        get() = File(rootDir, SEARCH_DIRECTORY).also { it.mkdirs() }

    override fun get(id: Id): File = getItemFile("${id.fileName}.$fileExtension")

    override fun getItemFile(fileName: String): File = File(itemsDirectory, fileName)

    override val summaryFile: File
        get() = File(summaryDirectory, "$SUMMARIES_FILE.$fileExtension")

    override val syncSummaryFile: File
        get() = File(summaryDirectory, "$SYNC_SUMMARIES_FILE.$fileExtension")

    override val dataChangeHistorySummaryFile: File
        get() = File(summaryDirectory, "$DATA_CHANGE_HISTORY_SUMMARIES_FILE.$fileExtension")

    override val sharingItemGroupsFile: File
        get() = File(sharingDirectory, "$ITEM_GROUPS_FILE.$fileExtension")

    override val sharingUserGroupsFile: File
        get() = File(sharingDirectory, "$USER_GROUPS_FILE.$fileExtension")

    override val sharingItemContentsFile: File
        get() = File(sharingDirectory, "$ITEM_CONTENTS_FILE.$fileExtension")

    override val sharingCollectionsFile: File
        get() = File(sharingDirectory, "$SHARING_COLLECTIONS_FILE.$fileExtension")

    override val backupDirectory: File
        get() = File(rootDir, BACKUPS_DIRECTORY).also { it.mkdirs() }

    override val searchQueryFile: File
        get() = File(searchDirectory, "$SEARCH_QUERY_FILE.$fileExtension")

    override fun getBackupFile(id: Id): File =
        getBackupFile("${id.fileName}.$fileExtension")

    override fun getBackupFile(fileName: String): File =
        File(backupDirectory, fileName)
}

private class TransactionImpl(val transactionRootDir: File, val content: Content) :
    Content by content, TransactionDelete {
    override val deleteDirectory: File
        get() = File(
            transactionRootDir,
            "$TRANSACTION_DELETE_DIRECTORY/$ITEMS_DIRECTORY",
        ).also { it.mkdirs() }

    override fun getFileToDelete(id: Id): File {
        return File(deleteDirectory, "${id.fileName}.${content.fileExtension}")
    }
}

@Suppress("FunctionName")
internal fun DatabaseFiles(rootDir: File, databaseName: String): DatabaseFiles =
    DatabaseFilesImpl(rootDir, databaseName)

internal class DatabaseFilesImpl(
    rootDir: File,
    databaseName: String,
    private val fileExtension: String = AES_JSON_FILE_EXTENSION,
) : DatabaseFiles {
    private val file = File(rootDir, databaseName).also { it.mkdirs() }
    private val contentRoot: File
        get() = File(file, CONTENT_DIRECTORY).also { it.mkdirs() }
    private val transactionContentRoot: File
        get() = File(transactionRoot, TRANSACTION_UPDATE_DIRECTORY)
    private val transaction: TransactionImpl
        get() = TransactionImpl(transactionRoot, ContentImpl(transactionContentRoot, fileExtension))

    private val transactionSharing: TransactionImpl
        get() = TransactionImpl(
            transactionSharingRoot,
            ContentImpl(transactionSharingRoot, fileExtension),
        )
    private val transactionSearch: TransactionImpl
        get() = TransactionImpl(
            transactionSearchRoot,
            ContentImpl(transactionSearchRoot, fileExtension),
        )

    override val transactionRoot: File
        get() = File(file, TRANSACTION_DIRECTORY)
    private val transactionSharingRoot: File
        get() = File(file, TRANSACTION_SHARING_DIRECTORY)
    private val transactionSearchRoot: File
        get() = File(file, TRANSACTION_SEARCH_DIRECTORY)
    override val content: Content
        get() = ContentImpl(contentRoot, fileExtension)
    override val transactionContent: Content
        get() = transaction
    override val transactionDelete: TransactionDelete
        get() = transaction
    override val index: File
        get() = File(file, "$INDEX_FILE.$fileExtension")
    override val transactionSharingContent: Content
        get() = transactionSharing
    override val transactionSearchContent: Content
        get() = transactionSearch
    override val marker: Marker
        get() = Marker(
            vault = File(file, TRANSACTION_MARKER),
            sharing = File(file, TRANSACTION_SHARING_MARKER),
            search = File(file, TRANSACTION_SEARCH_MARKER),
        )

    override fun delete() =
        file.deleteRecursively()

    override fun rename(newDatabaseName: String) =
        file.renameTo(File(file.parentFile, newDatabaseName))
}

internal data class Marker(val vault: File, val sharing: File, val search: File)

internal fun File.isDirectoryEmpty(): Boolean {
    if (isDirectory) {
        val files: Array<String> = list() ?: return true
        return files.isEmpty()
    } else {
        return length() == 0L
    }
}