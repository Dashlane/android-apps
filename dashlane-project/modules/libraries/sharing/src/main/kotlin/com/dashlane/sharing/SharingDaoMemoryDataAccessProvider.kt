package com.dashlane.sharing

interface SharingDaoMemoryDataAccessProvider {
    suspend fun create(): SharingDaoMemoryDataAccess
}