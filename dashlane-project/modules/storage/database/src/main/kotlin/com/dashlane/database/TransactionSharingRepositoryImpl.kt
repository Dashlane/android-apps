package com.dashlane.database

import java.io.File

internal class TransactionSharingRepositoryImpl(
    private val files: DatabaseFiles,
) : TransactionRepository {
    override val markerFile: File
        get() = files.marker.sharing

    override suspend fun withMarker(block: suspend () -> Unit) {
        if (markerExist) throw IllegalStateException("Should not happen. Transaction already exists")
        createMarker()
        block.invoke()
        releaseMarker()
    }

    override fun releaseMarker() {
        markerFile.delete()
        files.transactionSharingContent.delete()
    }

    private fun createMarker() = markerFile.createNewFile()
}