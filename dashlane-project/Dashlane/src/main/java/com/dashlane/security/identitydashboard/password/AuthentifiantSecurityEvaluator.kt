package com.dashlane.security.identitydashboard.password

import androidx.annotation.VisibleForTesting
import com.dashlane.breach.Breach
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.security.identitydashboard.SecurityScore
import com.dashlane.similarpassword.GroupOfPassword
import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.manager.DataIdentifierSpaceCategorization
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.url.UrlDomain
import com.dashlane.url.registry.UrlDomainCategory
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.JsonSerialization
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.Cache
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.obfuscated.isNullOrEmpty
import com.dashlane.util.time.TimeMeasurement
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.leakedPasswordsSet
import com.dashlane.vault.model.toVaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthentifiantSecurityEvaluator @Inject constructor(
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val similarPassword: SimilarPassword,
    @Cache
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val jsonSerialization: JsonSerialization,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val urlDomainRegistryFactory: UrlDomainRegistryFactory,
    private val mainDataAccessor: MainDataAccessor
) {
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()

    suspend fun computeResult(
        teamspace: Teamspace,
        ignoreUserLock: Boolean = false
    ): Result = withContext(ioDispatcher) {
        computeResult(
            getAuthentifiants(
                vaultDataQuery,
                ignoreUserLock
            ).map { it.toAnalyzedAuthentifiant() },
            getSecurityBreaches(
                vaultDataQuery,
                ignoreUserLock
            ).distinctBy { it.breachId },
            teamspace
        )
    }

    @Suppress("LongMethod")
    @VisibleForTesting
    suspend fun computeResult(
        itemsAllSpaces: List<AnalyzedAuthentifiant>,
        securityBreaches: List<SyncObject.SecurityBreach>,
        teamspace: Teamspace
    ): Result {
        val timeMeasurement = TimeMeasurement("SecurityEvaluator")
        val passwordsAllSpaces = getPasswordsForAllSpaces(itemsAllSpaces)
        val spaceCategorization =
            teamspaceAccessorProvider.get()
                ?.let { DataIdentifierSpaceCategorization(it, teamspace) }
        val items =
            itemsAllSpaces.filter { spaceCategorization?.canBeDisplay(it.item.toVaultItem()) == true }
        val allDomainsUrls = items.mapNotNull { it.navigationUrl }
        timeMeasurement.tick("filter1")
        val groupOfPasswordsAllSpaces =
            GroupOfPassword.Builder(similarPassword).compute(passwordsAllSpaces.toTypedArray())
        timeMeasurement.tick("grouping")
        val countByPasswordStrength: MutableMap<PasswordStrengthScore, Int> = mutableMapOf()
        val ignoredAuthentifiants = items.filter { it.checked }.sortedWith(byAlphabetic())
        val toCheckAuthentifiants = items.filter { !it.checked }
        val toCheckAuthentifiantsAllSpaces = itemsAllSpaces.filter { !it.checked }
        val passwordsToMeasureStrength = toCheckAuthentifiants.map { it.password }
        timeMeasurement.tick("filter2")
        val strengthByPasswords =
            runCatching {
                passwordStrengthEvaluator.associatePasswordsWithPasswordStrengthScore(
                    passwordsToMeasureStrength
                )
            }.getOrDefault(emptyMap())
        timeMeasurement.tick("measureStrength")
        toCheckAuthentifiants.forEach { item ->
            item.password
                .let { strengthByPasswords[it] }
                ?.let {
                    countByPasswordStrength[it] = (countByPasswordStrength[it] ?: 0) + 1
                }
        }
        val totalAccount = toCheckAuthentifiants.count()
        val authentifiantsByStrength =
            getAuthentifiantsByStrength(
                countByPasswordStrength,
                toCheckAuthentifiants,
                strengthByPasswords
            )
        val authentifiantsBySimilarity = getAuthentifiantsBySimilarity(
            groupOfPasswordsAllSpaces,
            toCheckAuthentifiantsAllSpaces,
            toCheckAuthentifiants
        )
        val authentifiantsBySecurityBreach =
            getAuthentifiantsBySecurityBreach(securityBreaches, toCheckAuthentifiants)
        timeMeasurement.tick("mapping")
        
        val authentifiantCorrupted: Set<AnalyzedAuthentifiant>
        val resultStrength = extractAuthentifiant(authentifiantsByStrength)
        val resultSimilarity = extractAuthentifiant(authentifiantsBySimilarity)
        val resultBreach = extractAuthentifiant(authentifiantsBySecurityBreach)
        timeMeasurement.tick("extractPerTab")
        authentifiantCorrupted = (resultStrength + resultSimilarity + resultBreach).toSet()
        val allCorrupted = authentifiantCorrupted.count()
        
        val urlDomainRegistry = urlDomainRegistryFactory.create()
        val sensitiveDomainCached =
            urlDomainRegistry.associateDomainsWithCategory(allDomainsUrls.mapNotNull(String::toUrlDomainOrNull))
        timeMeasurement.tick("loadSensitiveDomains")
        val importantCorrupted =
            authentifiantCorrupted.count {
                sensitiveDomainCached[it.navigationUrl?.toUrlDomainOrNull()]?.isDataSensitive
                    ?: false
            }
        
        val securityScore = getSecurityScore(allCorrupted, importantCorrupted, totalAccount)
        timeMeasurement.tick("computeSecurityScore")
        timeMeasurement.tick("computeUL12")
        val totalSafeCredentials = countSafeCredentials(
            toCheckAuthentifiants,
            authentifiantsByStrength,
            authentifiantsBySimilarity,
            authentifiantsBySecurityBreach
        )
        return Result(
            securityScore,
            items.size,
            totalSafeCredentials,
            authentifiantsByStrength,
            authentifiantsBySimilarity,
            authentifiantsBySecurityBreach,
            ignoredAuthentifiants,
            sensitiveDomainCached,
            timeMeasurement
        )
    }

    private fun countSafeCredentials(
        toCheckAuthentifiants: List<AnalyzedAuthentifiant>,
        authentifiantsByStrength: List<GroupOfAuthentifiant<PasswordStrengthScore>>,
        authentifiantsBySimilarity: List<GroupOfAuthentifiant<GroupOfPassword>>,
        authentifiantsBySecurityBreach: List<GroupOfAuthentifiant<Breach>>
    ) = toCheckAuthentifiants
        .count { authentifiantToCheck ->
            authentifiantsByStrength.none { it.authentifiants.contains(authentifiantToCheck) } &&
                authentifiantsBySimilarity.none { it.authentifiants.contains(authentifiantToCheck) } &&
                authentifiantsBySecurityBreach.none {
                    it.authentifiants.contains(
                        authentifiantToCheck
                    )
                }
        }

    private fun getAuthentifiantsBySecurityBreach(
        securityBreaches: List<SyncObject.SecurityBreach>,
        toCheckAuthentifiants: List<AnalyzedAuthentifiant>
    ) = securityBreaches.mapNotNull { securityBreach ->
        val leakedPasswordsSet = securityBreach.leakedPasswordsSet
        toCheckAuthentifiants
            .filter { authentifiant ->
                authentifiant.password.let { authentifiantPassword ->
                    leakedPasswordsSet.any { leakPassword ->
                        similarPassword.areSimilar(authentifiantPassword, leakPassword)
                    }
                }
            }
            .takeIf { it.isNotEmpty() }
            ?.sortedWith(byAlphabetic())
            ?.let { items ->
                extractBreach(securityBreach)?.let { breach ->
                    GroupOfAuthentifiant(breach, items)
                }
            }
    }

    private fun getAuthentifiantsBySimilarity(
        groupOfPasswordsAllSpaces: List<GroupOfPassword>,
        toCheckAuthentifiantsAllSpaces: List<AnalyzedAuthentifiant>,
        toCheckAuthentifiants: List<AnalyzedAuthentifiant>
    ) = groupOfPasswordsAllSpaces.mapNotNull { groupOfPassword ->
        val totalAffectedAllSpaces =
            toCheckAuthentifiantsAllSpaces.count {
                groupOfPassword.contains(it.password)
            }

        toCheckAuthentifiants
            .takeIf { totalAffectedAllSpaces >= MIN_SIZE_FOR_REUSED }
            ?.filter { groupOfPassword.contains(it.password) }
            ?.sortedWith(byAlphabetic())
            ?.let { GroupOfAuthentifiant(groupOfPassword, it, countReal = totalAffectedAllSpaces) }
    }

    private fun getAuthentifiantsByStrength(
        countByPasswordStrength: MutableMap<PasswordStrengthScore, Int>,
        toCheckAuthentifiants: List<AnalyzedAuthentifiant>,
        strengthByPasswords: Map<String, PasswordStrengthScore?>
    ) = countByPasswordStrength.keys.mapNotNull { strength ->
        strength.takeIf { it <= MIN_STRENGTH_SCORE_FOR_DASHBOARD }
            ?.let {
                toCheckAuthentifiants
                    .filter { strengthByPasswords[it.password] == strength }
                    .sortedWith(byAlphabetic())
            }
            ?.let { GroupOfAuthentifiant(strength, it) }
    }

    private fun getPasswordsForAllSpaces(itemsAllSpaces: List<AnalyzedAuthentifiant>) =
        itemsAllSpaces.mapNotNull {
            it.password.takeIf { !it.isBlank() }
        }

    private fun extractAuthentifiant(items: List<GroupOfAuthentifiant<*>>) =
        items.map { it.authentifiants }.flatten()

    private fun extractBreach(securityBreach: SyncObject.SecurityBreach) =
        tryOrNull { jsonSerialization.fromJson(securityBreach.content, Breach::class.java) }

    private suspend fun getAuthentifiants(vaultDataQuery: VaultDataQuery, ignoreUserLock: Boolean) =
        withContext(ioDispatcher) {
            
            vaultDataQuery.queryAll(
                vaultFilter {
                    if (ignoreUserLock) ignoreUserLock()
                    specificDataType(SyncObjectType.AUTHENTIFIANT)
                }
            ).mapNotNull {
                val syncObject = it.syncObject as SyncObject.Authentifiant

                if (!syncObject.password.isNullOrEmpty()) {
                    syncObject
                } else {
                    null
                }
            }
        }

    private suspend fun getSecurityBreaches(
        vaultDataQuery: VaultDataQuery,
        ignoreUserLock: Boolean
    ): List<SyncObject.SecurityBreach> = withContext(ioDispatcher) {
        val filter = vaultFilter {
            if (ignoreUserLock) ignoreUserLock()
            specificDataType(SyncObjectType.SECURITY_BREACH)
        }
        vaultDataQuery.queryAll(filter).mapNotNull {
            it.syncObject as? SyncObject.SecurityBreach
        }
    }

    private fun getSecurityScore(
        allCorrupted: Int,
        importantCorrupted: Int,
        totalAccount: Int
    ) = SecurityScore.getSecurityScore(allCorrupted, importantCorrupted, totalAccount)

    private fun byAlphabetic() = byTitle().then(byLogin())

    private fun byTitle(): Comparator<AnalyzedAuthentifiant> {
        return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.titleForListNormalized }
    }

    private fun byLogin(): Comparator<AnalyzedAuthentifiant> {
        return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.loginForUi }
    }

    data class Result(
        val securityScore: SecurityScore?,
        val totalCredentials: Int,
        val totalSafeCredentials: Int,
        val authentifiantsByStrength: List<GroupOfAuthentifiant<PasswordStrengthScore>>,
        val authentifiantsBySimilarity: List<GroupOfAuthentifiant<GroupOfPassword>>,
        val authentifiantsByBreach: List<GroupOfAuthentifiant<Breach>>,
        val authentifiantsIgnored: List<AnalyzedAuthentifiant>,
        val sensitiveDomains: Map<UrlDomain, UrlDomainCategory?>,
        val timeMeasurement: TimeMeasurement
    )

    companion object {
        const val MIN_SIZE_FOR_REUSED = 2
        val MIN_STRENGTH_SCORE_FOR_DASHBOARD = PasswordStrengthScore.VERY_GUESSABLE
    }
}
