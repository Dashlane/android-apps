package com.dashlane.csvimport.csvimport

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.csvimport.utils.csvLineSequence
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.PackageUtilities
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject

class CsvImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mainDataAccessor: MainDataAccessor,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val knownApplicationProvider: KnownApplicationProvider,
) {
    private lateinit var uri: Uri
    val inputStreamProvider: () -> InputStream
        get() = { context.contentResolver.openInputStream(uri)!! }

    fun setFileUri(uri: Uri) {
        this.uri = uri
    }

    fun inferSchema(): CsvSchema? = inputStreamProvider()
        .reader()
        .useLines { lines ->
            lines.firstOrNull().let { if (it == null) emptySchema else firstLineToSchema[it] }
        }

    fun loadCredentials(schema: CsvSchema): List<CsvAuthentifiant> {
        val appNameFromPackage = { packageName: String ->
            PackageUtilities.getApplicationNameFromPackage(context, packageName)
        }

        val foundCredentials = inputStreamProvider().reader().use { reader ->
            reader.csvLineSequence(separator = schema.separator)
                .drop(if (schema.hasHeader) 1 else 0)
                .mapNotNull {
                    newCsvAuthentifiant(
                        linkedServicesHelper,
                        knownApplicationProvider,
                        it,
                        schema.fieldTypes,
                        appNameFromPackage
                    )
                }
                .toList()
        }

        val filter = vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
        }
        val allAuthentifiants = mainDataAccessor.getVaultDataQuery()
            .queryAll(filter)
            .filterIsInstance<VaultItem<SyncObject.Authentifiant>>()
            .map { it.syncObject }

        return foundCredentials.filterNew(allAuthentifiants)
    }

    fun selectFields(): Pair<Char, List<String>>? = possibleCsvSeparators
        .asSequence()
        .map { separator ->
            separator to inputStreamProvider()
                .reader()
                .use { reader ->
                    reader.csvLineSequence(separator = separator)
                        .take(CSV_FIRST_LINES_THRESHOLD)
                        .toList()
                }
        }
        .firstOrNull { (_, firstLines) ->
            hasCoherentColumnSize(firstLines)
        }?.let { (separator, firstLines) ->
            separator to firstLines.last()
        }

    private fun hasCoherentColumnSize(lines: List<List<String>>): Boolean {
        
        
        val firstLineSize = lines.firstOrNull()?.size ?: 0
        return firstLineSize > 1 && lines.all { it.size == firstLineSize }
    }

    companion object {
        
        private const val CSV_FIRST_LINES_THRESHOLD = 3
        private val possibleCsvSeparators = listOf(',', ';', '\t', '|')

        
        val chromeSchema = CsvSchema(
            hasHeader = true,
            separator = ',',
            fieldTypes = listOf(
                null,
                CsvSchema.FieldType.URL,
                CsvSchema.FieldType.USERNAME,
                CsvSchema.FieldType.PASSWORD
            )
        )

        
        val emptySchema = CsvSchema(
            hasHeader = false,
            separator = ',',
            fieldTypes = emptyList()
        )

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val firstLineToSchema = mapOf(
            "name,url,username,password" to chromeSchema
        )
    }
}