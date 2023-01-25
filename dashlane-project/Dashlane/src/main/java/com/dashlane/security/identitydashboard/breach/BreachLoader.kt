package com.dashlane.security.identitydashboard.breach

import androidx.annotation.VisibleForTesting
import com.dashlane.breach.Breach
import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.JsonSerialization
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject



class BreachLoader @Inject constructor(
    private val jsonConverter: JsonSerialization,
    private val mainDataAccessor: MainDataAccessor
) {
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()

    fun getBreachesWrapper(limit: Int?): List<BreachWrapper> {
        return getBreachesWrapper(loadBreachesFromDB(), limit)
    }

    fun getBreachesWrapper(ignoreUserLock: Boolean = false): List<BreachWrapper> {
        return getBreachesWrapper(loadBreachesFromDB(ignoreUserLock = ignoreUserLock), ignoreUserLock = ignoreUserLock)
    }

    fun getBreachesWrapper(
        breaches: List<VaultItem<SyncObject.SecurityBreach>>,
        limit: Int? = null,
        ignoreUserLock: Boolean = false
    ): List<BreachWrapper> {
        val authentifiants = getAllCredentials(ignoreUserLock)
        return getBreachesWrapper(breaches, authentifiants, limit)
    }

    @VisibleForTesting
    fun getBreachesWrapper(
        breaches: List<VaultItem<SyncObject.SecurityBreach>>,
        authentifiants: List<SyncObject.Authentifiant>,
        limit: Int? = null
    ): List<BreachWrapper> {

        val securityBreaches = getSecurityBreachWithPublicBreach(distinct(breaches.map { it.toAnalyzedBreach() }))
        val similarPassword = SimilarPassword()

        val breachWrappers: MutableList<BreachWrapper> = mutableListOf()

        securityBreaches
            .sortedByDescending { (_, publicBreach) ->
                publicBreach.breachCreationDate
            }.forEach { (localBreach, publicBreach) ->
                
                if (limit != null && breachWrappers.size >= limit) return@forEach
                
                val isPopupDisplayable = localBreach.status == SyncObject.SecurityBreach.Status.PENDING

                
                
                val forThisBreach = authentifiants.filter { isAffected(it, localBreach, similarPassword) }

                if (publicBreach.isDarkWebBreach() || forThisBreach.isNotEmpty()) {
                    breachWrappers.add(
                        BreachWrapper(localBreach, publicBreach, forThisBreach.mapNotNull { it.id }, isPopupDisplayable)
                    )
                }
            }

        return breachWrappers
    }

    @VisibleForTesting
    fun distinct(breaches: List<AnalyzedBreach>): List<AnalyzedBreach> {
        val byBreachId = mutableMapOf<String, AnalyzedBreach>()
        breaches.forEach { breach ->
            val key = breach.breachId ?: ""
            val alreadyInMap = byBreachId[key]
            if (alreadyInMap == null) {
                
                byBreachId[key] = breach
            } else {
                
                byBreachId[key] =
                    alreadyInMap.copy(leakedPasswordsSet = alreadyInMap.leakedPasswordsSet + breach.leakedPasswordsSet)
            }
        }
        return byBreachId.values.toList()
    }

    private fun getSecurityBreachWithPublicBreach(breaches: List<AnalyzedBreach>) =
        breaches.mapNotNull { securityBreach -> toBreach(securityBreach)?.let { securityBreach to it } }

    @Suppress("UNCHECKED_CAST")
    private fun loadBreachesFromDB(ignoreUserLock: Boolean = false): List<VaultItem<SyncObject.SecurityBreach>> {
        val filter = vaultFilter {
            if (ignoreUserLock) ignoreUserLock()
            specificDataType(SyncObjectType.SECURITY_BREACH)
        }
        val breachStatusFilter = arrayOf(
            SyncObject.SecurityBreach.Status.PENDING,
            SyncObject.SecurityBreach.Status.VIEWED,
            SyncObject.SecurityBreach.Status.SOLVED
        )
        return vaultDataQuery.queryAll(filter)
            .filterIsInstance<VaultItem<SyncObject.SecurityBreach>>()
            .filter { it.syncObject.status in breachStatusFilter }
    }

    private fun getAllCredentials(ignoreUserLock: Boolean): List<SyncObject.Authentifiant> {
        val filter = vaultFilter {
            if (ignoreUserLock) ignoreUserLock()
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            forCurrentSpace()
        }
        return vaultDataQuery.queryAll(filter).map { it.syncObject }.filterIsInstance<SyncObject.Authentifiant>()
    }

    private fun toBreach(localBreach: AnalyzedBreach) =
        tryOrNull { jsonConverter.fromJson(localBreach.content, Breach::class.java) }

    @VisibleForTesting
    fun isAffected(
        authentifiant: SyncObject.Authentifiant,
        localBreach: AnalyzedBreach,
        similarPassword: SimilarPassword
    ): Boolean {
        if (localBreach.leakedPasswordsSet.isEmpty()) {
            return false
        }
        if (authentifiant.checked == true) {
            return false 
        }
        val authentifiantPassword = authentifiant.password?.toString()
        if (authentifiantPassword.isNullOrEmpty()) {
            return false
        }
        return localBreach.leakedPasswordsSet.any { similarPassword.areSimilar(authentifiantPassword, it) }
    }
}