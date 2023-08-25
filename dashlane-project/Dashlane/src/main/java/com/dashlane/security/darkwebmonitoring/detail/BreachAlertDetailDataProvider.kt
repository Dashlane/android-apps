package com.dashlane.security.darkwebmonitoring.detail

import com.dashlane.notificationcenter.alerts.BreachDataHelper
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class BreachAlertDetailDataProvider @Inject constructor(
    private val breachesDataHelper: BreachDataHelper,
    private val breachLoader: BreachLoader,
    private val mainDataAccessor: MainDataAccessor,
) : BaseDataProvider<BreachAlertDetail.Presenter>(), BreachAlertDetail.DataProvider {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    override suspend fun deleteBreach(breachWrapper: BreachWrapper) {
        breachesDataHelper.saveAndRemove(breachWrapper, SyncObject.SecurityBreach.Status.ACKNOWLEDGED)
    }

    override suspend fun restoreBreach(breachWrapper: BreachWrapper) {
        breachesDataHelper.saveAndRemove(breachWrapper, breachWrapper.localBreach.status!!)
    }

    override suspend fun markAsViewed(breachWrapper: BreachWrapper) {
        breachesDataHelper.saveAndRemove(breachWrapper, SyncObject.SecurityBreach.Status.VIEWED)
    }

    override suspend fun markAsResolved(breachWrapper: BreachWrapper) {
        breachesDataHelper.saveAndRemove(breachWrapper, SyncObject.SecurityBreach.Status.SOLVED)
    }

    override suspend fun getDarkwebBreaches(): List<BreachWrapper> =
        breachLoader.getBreachesWrapper().filter {
            it.publicBreach.isDarkWebBreach()
        }

    override suspend fun getCredential(itemId: String): SummaryObject.Authentifiant? {
        val vaultItem = genericDataQuery.queryFirst(
            vaultFilter {
            specificDataType(SyncObjectType.AUTHENTIFIANT)
            specificUid(itemId)
        }
        ) ?: return null
        return vaultItem as SummaryObject.Authentifiant
    }
}