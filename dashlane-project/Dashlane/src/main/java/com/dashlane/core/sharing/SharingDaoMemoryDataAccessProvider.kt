package com.dashlane.core.sharing

interface SharingDaoMemoryDataAccessProvider {
    suspend fun create(): SharingDaoMemoryDataAccess
}