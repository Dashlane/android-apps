package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.DataStorageProvider
import javax.inject.Inject

class MainDataAccessorImpl @Inject constructor(
    private val dataSaver: DataSaver,
    private val generatedPasswordQuery: GeneratedPasswordQueryImpl,
    private val dataStorageProvider: DataStorageProvider
) : MainDataAccessor {

    override fun getDataSaver() = dataSaver

    override fun getGenericDataQuery() = dataStorageProvider.genericDataQuery

    override fun getCredentialDataQuery() = dataStorageProvider.credentialDataQuery

    override fun getCollectionDataQuery() = dataStorageProvider.collectionDataQuery

    override fun getDataChangeHistoryQuery() = dataStorageProvider.dataChangeHistoryQuery

    override fun getGeneratedPasswordQuery() = generatedPasswordQuery

    override fun getDataCounter() = dataStorageProvider.dataCounter

    override fun getFrequentSearch() = dataStorageProvider.frequentSearch

    override fun getVaultDataQuery() = dataStorageProvider.vaultDataQuery
}