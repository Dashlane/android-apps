package com.dashlane.hermes.storage

interface LogStorage {
    fun storeLog(logItem: LogItem)

    fun readLogs(storageCategory: StorageCategory): List<LogItem>

    fun deleteLogs(logItems: List<LogItem>)
}