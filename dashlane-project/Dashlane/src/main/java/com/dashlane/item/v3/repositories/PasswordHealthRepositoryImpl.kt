package com.dashlane.item.v3.repositories

import com.dashlane.item.v3.data.PasswordHealthData
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.util.SecurityBreachUtil.isCompromised
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class PasswordHealthRepositoryImpl @Inject constructor(
    private val itemEditRepository: ItemEditRepository,
    private val vaultDataQuery: VaultDataQuery,
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator
) : PasswordHealthRepository {
    override suspend fun getPasswordHealth(
        itemId: String?,
        password: String?
    ): PasswordHealthData {
        val loadedPassword = password ?: itemId?.let { uid ->
            vaultDataQuery.query(vaultFilter { specificUid(uid) })
        }?.let {
            val syncObject = it.syncObject
            if (syncObject is SyncObject.Authentifiant) {
                return@let syncObject.password?.toString()
            }
            return@let null
        }
        if (loadedPassword.isNullOrEmpty()) {
            return PasswordHealthData(
                isCompromised = false,
                isPasswordEmpty = true,
                reusedCount = 0,
                passwordStrength = null
            )
        }
        val securityBreaches =
            vaultFilter { specificDataType(SyncObjectType.SECURITY_BREACH) }.let {
                vaultDataQuery.queryAll(it)
            }
        val isCompromised = securityBreaches.isCompromised(SimilarPassword(), loadedPassword)
        val passwordStrength = passwordStrengthEvaluator.getPasswordStrength(loadedPassword)
        val reusedCount = itemEditRepository.getPasswordReusedCount(loadedPassword)
        return PasswordHealthData(
            passwordStrength = passwordStrength,
            isCompromised = isCompromised,
            reusedCount = reusedCount,
            isPasswordEmpty = false
        )
    }
}