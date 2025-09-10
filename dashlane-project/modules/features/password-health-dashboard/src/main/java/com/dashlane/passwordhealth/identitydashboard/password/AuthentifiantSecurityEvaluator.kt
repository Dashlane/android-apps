package com.dashlane.passwordhealth.identitydashboard.password

import com.dashlane.passwordanalysis.AnalyzedAuthentifiant
import com.dashlane.passwordanalysis.SecurityScoreEvaluator
import com.dashlane.teamspaces.model.TeamSpace
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthentifiantSecurityEvaluator @Inject constructor(
    private val dataProvider: AuthentifiantSecurityDataProvider,
    private val evaluator: SecurityScoreEvaluator,
) {
    suspend fun computeResult(
        teamSpace: TeamSpace,
        ignoreUserLock: Boolean = false,
    ): Pair<SecurityScoreEvaluator.Result, List<AnalyzedAuthentifiant>> {
        val (itemsAllSpaces, itemsToAnalyse) = dataProvider.getAuthentifiantsForAnalysis(
            teamSpace = teamSpace,
            ignoreUserLock = ignoreUserLock,
        )
        return evaluator.computeResult(
            itemsToAnalyse = itemsToAnalyse,
            allItems = itemsAllSpaces,
            securityBreaches = dataProvider.getSecurityBreaches(ignoreUserLock = ignoreUserLock),
        ) to itemsToAnalyse
    }
}