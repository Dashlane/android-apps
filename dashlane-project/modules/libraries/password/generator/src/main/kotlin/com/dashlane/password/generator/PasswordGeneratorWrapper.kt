package com.dashlane.password.generator

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

interface PasswordGeneratorWrapper {
    fun getCriteria(username: String?): PasswordGeneratorCriteria

    fun saveCriteria(username: String, criteria: PasswordGeneratorCriteria)

    suspend fun generatePassword(username: String?, criteria: PasswordGeneratorCriteria?): String

    suspend fun saveToPasswordHistory(
        password: String,
        authDomain: String,
        authId: String,
    ): VaultItem<SyncObject.GeneratedPassword>
}