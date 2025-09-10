package com.dashlane.csvexport.exporter

import android.content.Context
import android.net.Uri
import com.dashlane.csvexport.model.ExportData
import com.dashlane.csvexport.model.buildExportVaultItem
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.space.SpecificSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.export.ExportVaultStatus
import com.dashlane.teamspaces.export.exportVaultStatus
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.FileUtils
import com.dashlane.util.UriUtils
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CSVExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoCoroutineDispatcher
    private val coroutineDispatcher: CoroutineDispatcher,
    private val vaultDataQuery: VaultDataQuery,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val fileUtils: FileUtils,
) {
    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()

    private val exportVaultStatus: ExportVaultStatus
        get() = teamSpaceAccessor?.exportVaultStatus ?: ExportVaultStatus.COMPLETE

    private val csvFileWriter = CSVFileWriter()

    companion object {
        private const val DASHLANE_CSV_FOLDER = "Dashlane CSV"
        private const val SUFFIX = ".csv"

        private const val FILE_NAME_CREDENTIALS = "credentials$SUFFIX"
        private const val FILE_NAME_SECURE_NOTES = "secureNotes$SUFFIX"
        private const val FILE_NAME_PAYMENTS = "payments$SUFFIX"
        private const val FILE_NAME_IDS = "ids$SUFFIX"
        private const val FILE_NAME_PERSONAL_INFO = "personalInfo$SUFFIX"
    }

    suspend fun export() {
        withContext(coroutineDispatcher) {
            val fallbackFiles = listOfNotNull(
                async { write(FILE_NAME_CREDENTIALS, SyncObjectType.AUTHENTIFIANT) }.await(),
                async { write(FILE_NAME_SECURE_NOTES, SyncObjectType.SECURE_NOTE) }.await(),
                async {
                    write(
                        FILE_NAME_PAYMENTS,
                        SyncObjectType.PAYMENT_CREDIT_CARD,
                        SyncObjectType.BANK_STATEMENT,
                    )
                }.await(),
                async {
                    write(
                        FILE_NAME_IDS,
                        SyncObjectType.ID_CARD,
                        SyncObjectType.PASSPORT,
                        SyncObjectType.DRIVER_LICENCE,
                        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
                        SyncObjectType.FISCAL_STATEMENT,
                    )
                }.await(),
                async {
                    write(
                        FILE_NAME_PERSONAL_INFO,
                        SyncObjectType.IDENTITY,
                        SyncObjectType.EMAIL,
                        SyncObjectType.PHONE,
                        SyncObjectType.ADDRESS,
                        SyncObjectType.COMPANY,
                        SyncObjectType.PERSONAL_WEBSITE,
                    )
                }.await(),
            )
            if (fallbackFiles.isNotEmpty()) {
                throw FallbackException(fallbackFiles)
            }
        }
    }

    private suspend fun write(fileName: String, vararg dataTypes: SyncObjectType): Uri? {
        return getItems(*dataTypes)
            .takeIf { it.isNotEmpty() }?.let {
                write(fileName, it)
            }
    }

    private suspend fun write(fileName: String, dataList: List<ExportData>): Uri? {
        runCatching {
            fileUtils.writeFileToPublicFolder(
                folder = DASHLANE_CSV_FOLDER,
                fileName = fileName,
                type = "application/octet-stream",
                size = null,
            ) { outputStream ->
                csvFileWriter.write(outputStream, dataList)
            }
            return null
        }.onFailure {
            
            return UriUtils.getOpenFileUri(
                context,
                fileUtils.writeFileToCacheFolder(fileName, "file_provider") { outputStream ->
                    csvFileWriter.write(outputStream, dataList)
                },
            )
        }
        return null
    }

    private suspend fun getItems(vararg dataTypes: SyncObjectType): List<ExportData> {
        return vaultDataQuery.queryAll(
            vaultFilter {
                specificDataType(*dataTypes)
                onlyShareable()
                if (exportVaultStatus == ExportVaultStatus.LIMITED) {
                    spaceFilter = SpecificSpaceFilter(listOf(TeamSpace.Personal))
                }
            },
        ).map {
            it.buildExportVaultItem()
        }
    }

    data class FallbackException(val files: List<Uri>) : Exception()
}