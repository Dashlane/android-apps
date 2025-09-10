package com.dashlane.database

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.CryptographyKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

@Suppress("FunctionName")
fun DatabaseProvider(
    rootDirAsync: Deferred<File>,
    cryptography: Cryptography,
    funcDatabaseName: (String) -> (String) = { it.databaseName() },
    ioDispatcher: CoroutineDispatcher,
): DatabaseProvider =
    DatabaseProviderImpl(rootDirAsync, cryptography, funcDatabaseName, ioDispatcher)

private class DatabaseProviderImpl(
    private val rootDirAsync: Deferred<File>,
    private val cryptography: Cryptography,
    private val funcDatabaseName: (String) -> (String),
    private val ioDispatcher: CoroutineDispatcher,
) : DatabaseProvider {
    private val databasePerSession = ConcurrentHashMap<String, Database>()

    private val lock = Mutex()

    override fun getDatabase(userName: String): Database? =
        databasePerSession[funcDatabaseName(userName)]

    override fun closeDatabase(userName: String) {
        (databasePerSession.remove(funcDatabaseName(userName)) as? DatabaseImpl)?.close()
    }

    override suspend fun open(userName: String, key: CryptographyKey.Raw32): Database {
        return withContext(ioDispatcher) {
            val databaseName = funcDatabaseName(userName)

            
            databasePerSession[databaseName] ?: lock.withLock {
                databasePerSession[databaseName] ?: if (exist(userName)) {
                    getDatabase(userName, key)
                } else {
                    createDatabase(userName, key)
                }.also {
                    databasePerSession[databaseName] = it
                }
            }
        }
    }

    private suspend fun databaseDirectory() = File(rootDirAsync.await(), DATABASES_FOLDER)

    private suspend fun getDatabase(
        userName: String,
        key: CryptographyKey.Raw32,
    ): DatabaseImpl {
        requireExist(userName)
        
        val files = DatabaseFiles(databaseDirectory(), funcDatabaseName(userName))

        
        val indexReader = IndexReader(files, cryptography)
        val index = try {
            indexReader.read(key)
        } catch (e: CryptographyException) {
            throw DatabaseCryptographyException(e)
        } ?: throw DatabaseIndexInvalidException()

        
        index.requireUserMatch(userName)

        
        val factory =
            CryptographyEngineFactory(cryptography, key)

        val reader = DatabaseReader(factory)
        val writer = DatabaseWriter(factory)

        val indexWriter = IndexWriter(files, writer)

        
        return DatabaseImpl(
            files = files,
            reader = reader,
            writer = writer,
            cryptographyEngineFactory = factory,
            indexReader = indexReader,
            indexWriter = indexWriter,
            ioDispatcher = ioDispatcher,
        ).apply {
            runCatching {
                recovery()
            }.onFailure {
                if (it is CancellationException) throw it
                throw DatabaseRecoveryException(cause = it)
            }
            runCatching {
                rebuildSummaryIfNeeded()
            }.onFailure {
                if (it is CancellationException) throw it
                throw DatabaseRebuildSummaryException(cause = it)
            }
            memorySummaryRepository.init()
        }
    }

    private suspend fun createDatabase(
        userName: String,
        key: CryptographyKey.Raw32,
    ): DatabaseImpl {
        requireNotExist(userName)
        
        val files = DatabaseFiles(databaseDirectory(), funcDatabaseName(userName))

        
        val factory =
            CryptographyEngineFactory(cryptography, key)

        
        val writer = DatabaseWriter(factory)

        
        val indexWriter = IndexWriter(files, writer)
        indexWriter.init(user = userName)

        
        val reader = DatabaseReader(factory)
        val indexReader = IndexReader(files, cryptography)

        
        return DatabaseImpl(files, reader, writer, factory, indexReader, indexWriter, ioDispatcher)
    }

    override suspend fun exist(userName: String): Boolean {
        val files = DatabaseFiles(databaseDirectory(), funcDatabaseName(userName))
        return files.index.exists()
    }

    override suspend fun delete(userName: String): Boolean =
        lock.withLock {
            val databaseName = funcDatabaseName(userName)
            withContext(ioDispatcher) {
                
                (databasePerSession.remove(databaseName) as? DatabaseImpl)?.delete()
                
                DatabaseFiles(databaseDirectory(), funcDatabaseName(userName)).delete()
                true
            }
        }

    override suspend fun rename(
        userName: String,
        newUserName: String,
        key: CryptographyKey.Raw32,
    ): Boolean =
        lock.withLock {
            withContext(ioDispatcher) {
                try {
                    val databaseName = funcDatabaseName(userName)
                    val newDatabaseName = funcDatabaseName(newUserName)
                    val files = DatabaseFiles(databaseDirectory(), databaseName)
                    if (files.index.exists()) {
                        
                        val factory = CryptographyEngineFactory(cryptography, key)
                        val writer = DatabaseWriter(factory)
                        val indexWriter = IndexWriter(files, writer)
                        indexWriter.init(user = newUserName)
                        
                        files.rename(newDatabaseName)
                    }
                    databasePerSession[newDatabaseName] = getDatabase(newUserName, key)
                    databasePerSession.remove(databaseName)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

    override suspend fun isAccessible(userName: String, key: CryptographyKey.Raw32): Boolean {
        return withContext(ioDispatcher) {
            
            val files = DatabaseFiles(databaseDirectory(), funcDatabaseName(userName))

            
            val indexReader = IndexReader(files, cryptography)
            runCatching { indexReader.read(key) }.getOrNull() != null
        }
    }

    private suspend fun requireExist(userName: String) {
        if (!exist(userName)) throw DatabaseIndexNotFoundException()
    }

    private suspend fun requireNotExist(userName: String) {
        if (exist(userName)) throw DatabaseAlreadyExistException()
    }

    companion object {
        private const val DATABASES_FOLDER = "databases"
    }
}

internal fun String.databaseName(): String =
    encodeUtf8().sha256().hex() + DatabaseFiles.DATABASE_EXTENSION