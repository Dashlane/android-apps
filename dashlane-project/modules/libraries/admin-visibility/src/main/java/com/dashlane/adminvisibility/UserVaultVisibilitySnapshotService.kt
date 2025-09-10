package com.dashlane.adminvisibility

import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.hilt.NitroProxy
import com.dashlane.passwordanalysis.AnalyzedAuthentifiant
import com.dashlane.passwordanalysis.SecurityScoreEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.uvvs.UploadUserSnapshotService
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.urlDomain
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider

class UserVaultVisibilitySnapshotService @Inject constructor(
    @NitroProxy
    private val nitro: Nitro,
    private val userFeaturesCheckerProvider: Provider<UserFeaturesChecker>,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
) {
    private val userFeaturesChecker: UserFeaturesChecker?
        get() = userFeaturesCheckerProvider.get()

    private val isUvvsReportEnabled: Boolean
        get() = teamSpaceAccessorProvider.get()?.currentBusinessTeam?.space?.teamInfo?.uvvsReportEnabled == true

    suspend fun trySend(
        authorization: Authorization.User,
        securityScoreResult: SecurityScoreEvaluator.Result,
        businessItems: List<AnalyzedAuthentifiant>,
    ) {
        if (!isUvvsReportEnabled) return
        if (userFeaturesChecker?.has(FeatureFlip.UVVS) != true) return

        uploadUvvs(
            authorization = authorization,
            uvvs = buildUvvs(
                securityScoreResult = securityScoreResult,
                businessItems = businessItems,
            ),
        )
    }

    private suspend fun uploadUvvs(
        authorization: Authorization.User,
        uvvs: List<UploadUserSnapshotService.Request.Uvv>,
    ) {
        runCatching {
            nitro.execute {
                endpoints.uvvs.uploadUserSnapshotService.execute(
                    userAuthorization = authorization,
                    request = UploadUserSnapshotService.Request(uvvs = uvvs),
                )
            }
        }.onFailure { throwable ->
        }
    }

    private fun buildUvvs(
        securityScoreResult: SecurityScoreEvaluator.Result,
        businessItems: List<AnalyzedAuthentifiant>,
    ): List<UploadUserSnapshotService.Request.Uvv> {
        val unsafeItemsIds: List<String?> = securityScoreResult.authentifiantsByStrength.filter {
            it.groupBy in arrayOf(
                PasswordStrengthScore.TOO_GUESSABLE,
                PasswordStrengthScore.VERY_GUESSABLE,
                PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            )
        }.flatMap { it.authentifiants }
            .mapNotNull { it.item.syncObject.id }
            .distinct()
        val breachedItemsIds: List<String?> = securityScoreResult.authentifiantsByBreach
            .flatMap { it.authentifiants }
            .mapNotNull { it.item.syncObject.id }
            .distinct()
        val reusedItemsIds: List<String?> = securityScoreResult.authentifiantsBySimilarity
            .flatMap { it.authentifiants }
            .mapNotNull { it.item.syncObject.id }
            .distinct()

        return businessItems.map { item ->
            val login = item.item.syncObject
            val itemId = item.item.syncObject.id

            UploadUserSnapshotService.Request.Uvv(
                creationDateUnix = (
                    login.creationDatetime?.epochSecond
                        ?: Instant.EPOCH.epochSecond
                ).toDouble(),
                risks = UploadUserSnapshotService.Request.Uvv.Risks(
                    isReused = itemId in reusedItemsIds,
                    isCompromised = itemId in breachedItemsIds,
                    isWeak = itemId in unsafeItemsIds,
                ),
                domain = item.item.urlDomain ?: "",
                id = itemId ?: "",
            )
        }
    }
}