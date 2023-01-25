package com.dashlane.storage.userdata.accessor

import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.Database
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy



@Singleton
class DataCounterImpl @Inject constructor(
    private val dataStorageProvider: Lazy<DataStorageProvider>,
    private val sessionManager: SessionManager,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val lockHelper: LockHelper
) : DataCounter, Database.OnUpdateListener {

    private val genericDataQuery: GenericDataQuery
        get() = dataStorageProvider.get().genericDataQuery

    private var lastUsernameCached: String? = null
    private val cachedValues = mutableMapOf<List<Teamspace>, MutableMap<SyncObjectType, Int>>()

    override fun onInsertOrUpdate(database: Database) {
        
        clearCache()
    }

    override fun count(filter: CounterFilter): Int {
        if (lockHelper.forbidDataAccess(filter)) return DataCounter.NO_COUNT

        resetCacheIfUsernameChanged()
        val teamspaceAccessor = teamspaceAccessorProvider.get() ?: return DataCounter.NO_COUNT
        val spacesRestrictions = filter.getSpacesRestrictions(teamspaceAccessor)?.asList() ?: emptyList()

        val cachedCountPerDataType = cachedValues.getOrPut(spacesRestrictions) { mutableMapOf() }

        return filter.dataTypeFilter.dataTypes.sumOf {
            cachedCountPerDataType.getOrPut(it) { getCountForDataType(it, filter) }
        }
    }

    private fun getCountForDataType(dataType: SyncObjectType, filter: CounterFilter): Int {
        val subFilter = filter.copy(dataTypeFilter = SpecificDataTypeFilter(dataType))
        return genericDataQuery.queryAll(subFilter).count()
    }

    private fun resetCacheIfUsernameChanged() {
        val username = sessionManager.session?.userId
        if (lastUsernameCached != username) {
            clearCache()
            lastUsernameCached = username
        }
    }

    private fun clearCache() {
        cachedValues.clear()
    }
}