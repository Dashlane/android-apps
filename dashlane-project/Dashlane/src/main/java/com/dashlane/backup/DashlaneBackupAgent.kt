package com.dashlane.backup

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import com.dashlane.cryptography.encodeUtf8ToByteArray
import com.dashlane.debug.DebugEntryPoint
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.toInt
import com.dashlane.util.tryOrNull
import dagger.hilt.android.EntryPointAccessors
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.Instant

@Suppress("UNUSED")
class DashlaneBackupAgent : BackupAgent() {
    private val globalPreferencesManager: GlobalPreferencesManager
        get() = EntryPointAccessors.fromApplication<DebugEntryPoint>(applicationContext).globalPreferencesManager

    companion object {
        private const val KEY_LAST_LOGGED_IN_USER = "lastLoggedInUser"
        private const val KEY_BACKUP_TOKEN = "backupToken"
        private const val KEY_BACKUP_TOKEN_DATE = "backupTokenDate"
    }

    override fun onFullBackup(data: FullBackupDataOutput?) {
        
        
        
        
    }

    override fun onRestoreFile(data: ParcelFileDescriptor?, size: Long, destination: File?, type: Int, mode: Long, mtime: Long) {
        
    }

    override fun onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput, newState: ParcelFileDescriptor) {
        val lastBackupTime =
            tryOrNull { DataInputStream(FileInputStream(oldState.fileDescriptor)).use { it.readLong() } } ?: 0L

        val lastModificationTime = globalPreferencesManager.lastModificationTime

        
        if (lastBackupTime != lastModificationTime) {
            
            backupGlobalPreferences(globalPreferencesManager, data)
        }
        saveLocalState(lastModificationTime, newState)
    }

    override fun onRestore(data: BackupDataInput, appVersionCode: Int, newState: ParcelFileDescriptor) {
        var backupToken: String? = null
        var backupTokenDate: Instant? = null
        while (data.readNextHeader()) {
            when (data.key) {
                KEY_LAST_LOGGED_IN_USER -> globalPreferencesManager.setLastLoggedInUser(data.readString())
                
                
                
                KEY_BACKUP_TOKEN -> backupToken = data.readString()
                KEY_BACKUP_TOKEN_DATE -> backupTokenDate = data.readInstant()
                
                else -> data.skipEntityData()
            }
        }
        
        val user = globalPreferencesManager.getDefaultUsername()
        if (backupToken != null && backupTokenDate != null && user != null) {
            globalPreferencesManager.setCipheredBackupToken(user, backupToken, backupTokenDate)
        }
        saveLocalState(System.currentTimeMillis(), newState)
    }

    private fun backupGlobalPreferences(
        prefManager: GlobalPreferencesManager,
        data: BackupDataOutput,
    ) {
        
        data.putString(KEY_LAST_LOGGED_IN_USER, prefManager.getLastLoggedInUser())
        val user = prefManager.getDefaultUsername()
        if (user != null) {
            prefManager.getCipheredBackupToken(user)?.let {
                
                data.putString(KEY_BACKUP_TOKEN, it)
                data.putInstant(KEY_BACKUP_TOKEN_DATE, prefManager.getBackupTokenDate(user))
            }
        }
    }

    private fun saveLocalState(backupTime: Long, newState: ParcelFileDescriptor) {
        
        DataOutputStream(FileOutputStream(newState.fileDescriptor)).use {
            
            it.writeLong(backupTime)
        }
    }
}

fun BackupDataInput.readData(): ByteArray {
    return ByteArrayInputStream(ByteArray(dataSize).also { readEntityData(it, 0, dataSize) }).use { data ->
        data.readBytes()
    }
}

fun BackupDataInput.readString() = String(readData())

fun BackupDataInput.readInstant(): Instant = Instant.ofEpochSecond(String(readData()).toLong())

fun BackupDataOutput.putString(key: String, value: String) {
    val data = value.encodeUtf8ToByteArray()
    val size = data.size
    writeEntityHeader(key, size)
    writeEntityData(data, size)
}

fun BackupDataOutput.putBoolean(key: String, value: Boolean) {
    val data = value.toInt()
    writeEntityHeader(key, 1)
    writeEntityData(byteArrayOf(data.toByte()), 1)
}

fun BackupDataOutput.putInstant(key: String, value: Instant) = putString(key, value.epochSecond.toString())