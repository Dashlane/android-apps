package com.dashlane.breach

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.breaches.GetBreachesService
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BreachServiceImpl @Inject constructor(
    private val gson: Gson,
    private val getBreachesService: GetBreachesService,
    private val getFileBreachesService: GetFileBreachesService,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager,
) : BreachService {

    override suspend fun getBreaches(
        fromRevision: Int,
        revisionOnly: Boolean
    ): BreachService.Result {
        val session = sessionManager.session ?: throw IllegalArgumentException("session is null")
        val response = withContext(ioDispatcher) {
            getBreachesService.execute(
                userAuthorization = session.authorization,
                request = GetBreachesService.Request(
                    revision = fromRevision.toDouble(),
                )
            )
        }

        if (revisionOnly) {
            return BreachService.Result(
                currentRevision = response.data.revision.toInt(),
                breaches = null,
            )
        }

        val breaches: List<BreachWithOriginalJson> = response.data.latestBreaches.mapNotNull(::toBreach)
        val files = response.data.filesToDownload ?: emptyList()
        val branchesFromFiles: List<BreachWithOriginalJson> = withContext(ioDispatcher) {
            files
                .map { url -> async { getFileBreachesService.execute(url) } }
                .awaitAll()
                .flatMap { it.breaches }
                .mapNotNull(::toBreach)
        }

        return BreachService.Result(
            currentRevision = response.data.revision.toInt(),
            breaches = mergeBreaches(breaches, branchesFromFiles),
        )
    }

    private fun toBreach(it: GetBreachesService.Data.LatestBreache): BreachWithOriginalJson? {
        val breach = it.toBreach() ?: return null
        val json = gson.toJson(breach)

        return BreachWithOriginalJson(breach, json)
    }

    private fun GetBreachesService.Data.LatestBreache.toBreach(): Breach? {
        return Breach(
            id = id ?: return null,
            breachModelVersion = breachModelVersion?.toInt() ?: 0,
            name = name,
            domains = domains,
            eventDate = eventDate,
            announcedDate = announcedDate,
            leakedData = leakedData,
            sensitiveDomain = sensitiveDomain ?: false,
            criticality = criticality?.toInt() ?: 0,
            restrictedArea = restrictedArea,
            relatedLinks = relatedLinks,
            template = template,
            status = status?.key,
            breachCreationDate = breachCreationDate?.toLong() ?: 0,
            lastModificationRevision = lastModificationRevision?.toInt() ?: 0
        )
    }

    private fun mergeBreaches(vararg lists: List<BreachWithOriginalJson>?): List<BreachWithOriginalJson> {
        return lists.filterNotNull()
            .flatten()
            .groupBy { it.breach.id }
            .values
            .mapNotNull { it.maxByOrNull { it.breach.lastModificationRevision } }
    }
}