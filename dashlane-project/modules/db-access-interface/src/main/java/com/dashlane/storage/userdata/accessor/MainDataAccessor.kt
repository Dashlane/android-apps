package com.dashlane.storage.userdata.accessor

import com.dashlane.vault.model.VaultItem



interface MainDataAccessor {

    fun getDataSaver(): DataSaver

    fun getGenericDataQuery(): GenericDataQuery

    fun getCredentialDataQuery(): CredentialDataQuery

    fun getCollectionDataQuery(): CollectionDataQuery

    fun getDataChangeHistoryQuery(): DataChangeHistoryQuery

    fun getGeneratedPasswordQuery(): GeneratedPasswordQuery

    fun getDataCounter(): DataCounter

    fun getFrequentSearch(): FrequentSearch

    fun getVaultDataQuery(): VaultDataQuery
}