package com.dashlane.passwordanalysis

import com.dashlane.breach.Breach
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.similarpassword.GroupOfPassword
import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.url.UrlDomain
import com.dashlane.url.registry.UrlDomainCategory
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.inject.qualifiers.Cache
import com.dashlane.util.time.TimeMeasurement
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.leakedPasswordsSet
import com.dashlane.xml.domain.SyncObject
import com.google.gson.Gson
import javax.inject.Inject

class SecurityScoreEvaluator @Inject constructor(
    @Cache
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val urlDomainRegistryFactory: UrlDomainRegistryFactory,
    private val gson: Gson,
) {
    @Suppress("LongMethod")
    suspend fun computeResult(
        itemsToAnalyse: List<AnalyzedAuthentifiant>,
        allItems: List<AnalyzedAuthentifiant>,
        securityBreaches: List<SyncObject.SecurityBreach>,
    ): Result {
        val timeMeasurement = TimeMeasurement("SecurityEvaluator")
        val similarPassword = SimilarPassword()
        val passwordsAllSpaces = getPasswordsForAllSpaces(allItems)
        val allDomainsUrls = itemsToAnalyse.mapNotNull { it.navigationUrl }
        timeMeasurement.tick("filter1")
        val groupOfPasswordsAllSpaces =
            GroupOfPassword.Builder(similarPassword).compute(passwordsAllSpaces.toTypedArray())
        timeMeasurement.tick("grouping")
        val countByPasswordStrength: MutableMap<PasswordStrengthScore, Int> = mutableMapOf()
        val ignoredAuthentifiants = itemsToAnalyse.filter { it.checked }.sortedWith(byAlphabetic())
        val toCheckAuthentifiants = itemsToAnalyse.filter { !it.checked }
        val toCheckAuthentifiantsAllSpaces = allItems.filter { !it.checked }
        val passwordsToMeasureStrength = toCheckAuthentifiants.map { it.password }
        timeMeasurement.tick("filter2")
        val strengthByPasswords = runCatching {
            passwordStrengthEvaluator.associatePasswordsWithPasswordStrengthScore(
                passwordsToMeasureStrength,
            )
        }.getOrElse { e ->
            emptyMap()
        }
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
                strengthByPasswords,
            )
        val authentifiantsBySimilarity = getAuthentifiantsBySimilarity(
            groupOfPasswordsAllSpaces,
            toCheckAuthentifiantsAllSpaces,
            toCheckAuthentifiants,
        )
        val authentifiantsBySecurityBreach =
            getAuthentifiantsBySecurityBreach(
                securityBreaches,
                toCheckAuthentifiants,
                similarPassword,
            )
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

        timeMeasurement.tick("computeSecurityScore")
        timeMeasurement.tick("computeUL12")
        val totalSafeCredentials = countSafeCredentials(
            toCheckAuthentifiants = toCheckAuthentifiants,
            authentifiantsByStrength = authentifiantsByStrength,
            authentifiantsBySimilarity = authentifiantsBySimilarity,
            authentifiantsBySecurityBreach = authentifiantsBySecurityBreach,
        )
        
        val securityScore = getSecurityScore(
            allCorrupted = allCorrupted,
            importantCorrupted = importantCorrupted,
            totalAccount = totalAccount,
        )
        return Result(
            securityScore = securityScore,
            totalCredentials = itemsToAnalyse.size,
            totalSafeCredentials = totalSafeCredentials,
            authentifiantsByStrength = authentifiantsByStrength,
            authentifiantsBySimilarity = authentifiantsBySimilarity,
            authentifiantsByBreach = authentifiantsBySecurityBreach,
            authentifiantsIgnored = ignoredAuthentifiants,
            sensitiveDomains = sensitiveDomainCached,
            timeMeasurement = timeMeasurement,
        )
    }

    private fun countSafeCredentials(
        toCheckAuthentifiants: List<AnalyzedAuthentifiant>,
        authentifiantsByStrength: List<GroupOfAuthentifiant<PasswordStrengthScore>>,
        authentifiantsBySimilarity: List<GroupOfAuthentifiant<GroupOfPassword>>,
        authentifiantsBySecurityBreach: List<GroupOfAuthentifiant<Breach>>,
    ) = toCheckAuthentifiants
        .count { authentifiantToCheck ->
            authentifiantsByStrength.none { it.authentifiants.contains(authentifiantToCheck) } &&
                authentifiantsBySimilarity.none { it.authentifiants.contains(authentifiantToCheck) } &&
                authentifiantsBySecurityBreach.none {
                    it.authentifiants.contains(
                        authentifiantToCheck,
                    )
                }
        }

    private fun getAuthentifiantsBySecurityBreach(
        securityBreaches: List<SyncObject.SecurityBreach>,
        toCheckAuthentifiants: List<AnalyzedAuthentifiant>,
        similarPassword: SimilarPassword,
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
        toCheckAuthentifiants: List<AnalyzedAuthentifiant>,
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
        strengthByPasswords: Map<String, PasswordStrengthScore?>,
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

    private fun byAlphabetic() = byTitle().then(byLogin())

    private fun extractBreach(securityBreach: SyncObject.SecurityBreach) =
        tryOrNull { gson.fromJson(securityBreach.content, Breach::class.java) }

    private fun byTitle(): Comparator<AnalyzedAuthentifiant> {
        return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.titleForListNormalized }
    }

    private fun byLogin(): Comparator<AnalyzedAuthentifiant> {
        return compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.loginForUi }
    }

    private fun getSecurityScore(
        allCorrupted: Int,
        importantCorrupted: Int,
        totalAccount: Int,
    ) = SecurityScore.getSecurityScore(allCorrupted, importantCorrupted, totalAccount)

    data class Result(
        val securityScore: SecurityScore?,
        val totalCredentials: Int,
        val totalSafeCredentials: Int,
        val authentifiantsByStrength: List<GroupOfAuthentifiant<PasswordStrengthScore>>,
        val authentifiantsBySimilarity: List<GroupOfAuthentifiant<GroupOfPassword>>,
        val authentifiantsByBreach: List<GroupOfAuthentifiant<Breach>>,
        val authentifiantsIgnored: List<AnalyzedAuthentifiant>,
        val sensitiveDomains: Map<UrlDomain, UrlDomainCategory?>,
        val timeMeasurement: TimeMeasurement,
    )

    companion object {
        const val MIN_SIZE_FOR_REUSED = 2
        val MIN_STRENGTH_SCORE_FOR_DASHBOARD = PasswordStrengthScore.VERY_GUESSABLE
    }
}