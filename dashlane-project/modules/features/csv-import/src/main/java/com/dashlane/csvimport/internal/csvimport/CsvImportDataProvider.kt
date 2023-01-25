package com.dashlane.csvimport.internal.csvimport

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.csvimport.ImportAuthentifiantHelper
import com.dashlane.csvimport.internal.CsvSchema
import com.dashlane.csvimport.internal.csvLineSequence
import com.dashlane.csvimport.internal.csvimport.CsvImportContract.State
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.util.PackageUtilities
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.provider.BaseDataProvider
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

@OptIn(ObsoleteCoroutinesApi::class)
internal class CsvImportDataProvider(
    initialState: State,
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val mainDataAccessor: MainDataAccessor,
    private val authentifiantHelper: ImportAuthentifiantHelper,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val inputStreamProvider: () -> InputStream,
) : BaseDataProvider<CsvImportContract.Presenter>(),
    CsvImportContract.DataProvider {
    private val _states = ConflatedBroadcastChannel(initialState)

    override val states: ReceiveChannel<State>
        get() = _states.openSubscription()

    override val currentState: State
        get() = _states.value

    override fun matchFields(types: List<CsvSchema.FieldType?>) {
        val state = _states.value as? State.Matching ?: return

        val schema = CsvSchema(
            hasHeader = false,
            separator = state.separator,
            fieldTypes = types
        )

        _states.trySend(State.Loading)

        coroutineScope.launch(Dispatchers.IO) {
            val nextState = try {
                State.Loaded(
                    loadAuthentifiants(schema),
                    false
                )
            } catch (_: Exception) {
                State.LoadingError
            }

            _states.trySend(nextState)
        }
    }

    override fun loadAuthentifiants() {
        if (_states.value !is State.Initial) return

        _states.trySend(State.Loading)

        coroutineScope.launch(Dispatchers.IO) {
            val nextState = try {
                val schema = inferSchema()

                if (schema == null) {
                    val (separator, fields) = selectFields(inputStreamProvider)!!

                    presenter.startMatchingFields(fields)

                    State.Matching(
                        fields,
                        separator
                    )
                } else {
                    State.Loaded(
                        loadAuthentifiants(schema),
                        false
                    )
                }
            } catch (e: Exception) {
                State.LoadingError
            }

            _states.trySend(nextState)
        }
    }

    override fun saveAuthentifiants() {
        val state = _states.value

        if (state !is State.Loaded || state.saving) return

        _states.trySend(state.copy(saving = true))

        coroutineScope.launch(Dispatchers.IO) {
            val nextState = try {
                val toSaveItems = state.authentifiants
                    .filter { it.selected }
                    .map { it.toVaultItem(authentifiantHelper) }
                val countSaved =
                    authentifiantHelper.addAuthentifiants(toSaveItems, UsageLogCode11.From.CHROME_IMPORT_CSV)
                State.Saved(countSaved)
            } catch (_: Exception) {
                state.copy(saving = false)
            }

            _states.trySend(nextState)
        }
    }

    override fun toggleAuthentifiantSelection(position: Int) {
        val state = _states.value

        if (state !is State.Loaded || state.saving) return

        val authentifiants = state.authentifiants.mapIndexed { index, csvAuthentifiant ->
            if (index == position) {
                csvAuthentifiant.copy(selected = !csvAuthentifiant.selected)
            } else {
                csvAuthentifiant
            }
        }

        val nextState = state.copy(authentifiants = authentifiants)
        _states.trySend(nextState)
    }

    @WorkerThread
    private fun inferSchema() = inputStreamProvider()
        .reader()
        .useLines { lines ->
            lines.firstOrNull().let { if (it == null) emptySchema else firstLineToSchema[it] }
        }

    @WorkerThread
    private fun loadAuthentifiants(schema: CsvSchema): List<CsvAuthentifiant> {
        val appNameFromPackage = { packageName: String ->
            PackageUtilities.getApplicationNameFromPackage(context, packageName)
        }

        val foundAuthentifiants = inputStreamProvider().reader().use { reader ->
            reader.csvLineSequence(separator = schema.separator)
                .drop(if (schema.hasHeader) 1 else 0)
                .mapNotNull { newCsvAuthentifiant(linkedServicesHelper, it, schema.fieldTypes, appNameFromPackage) }
                .toList()
        }

        val filter = vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
        }
        val allAuthentifiants = mainDataAccessor.getVaultDataQuery()
            .queryAll(filter)
            .filterIsInstance<VaultItem<SyncObject.Authentifiant>>()
            .map { it.syncObject }

        return foundAuthentifiants.filterNew(allAuthentifiants)
    }

    companion object {
        
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