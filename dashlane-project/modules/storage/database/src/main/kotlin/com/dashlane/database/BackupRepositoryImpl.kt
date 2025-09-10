package com.dashlane.database

import com.dashlane.database.adapter.XmlDataAdapter
import com.dashlane.xml.XmlData
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.asObjectNode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.buffer

internal class BackupRepositoryImpl(
    private val files: DatabaseFiles,
    private val reader: DatabaseReader,
    private val writer: DatabaseWriter,
    private val ioDispatcher: CoroutineDispatcher,
) : BackupRepository {
    private val xmlDataAdapter = XmlDataAdapter()

    override suspend fun save(id: Id, backup: XmlTransaction) {
        withContext(ioDispatcher) {
            val file = files.transactionContent.getBackupFile(id)
            writer.write(file) { sink ->
                xmlDataAdapter.toJson(sink, backup.node)
            }
        }
    }

    override suspend fun load(id: Id): XmlTransaction? {
        return withContext(ioDispatcher) {
            val file = files.content.getBackupFile(id)
            val transaction = reader.read(file) { source ->
                val xmlData: XmlData = xmlDataAdapter.fromJson(source.buffer()) ?: return@read null
                XmlTransaction(xmlData.asObjectNode())
            }
            return@withContext transaction
        }
    }
}