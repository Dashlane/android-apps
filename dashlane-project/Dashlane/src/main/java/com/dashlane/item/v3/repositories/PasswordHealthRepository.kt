package com.dashlane.item.v3.repositories

import com.dashlane.item.v3.data.PasswordHealthData

interface PasswordHealthRepository {
    suspend fun getPasswordHealth(
        itemId: String? = null,
        password: String? = null
    ): PasswordHealthData
}