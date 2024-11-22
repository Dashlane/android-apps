package com.dashlane.item.v3.loaders

import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.otp
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.loaders.common.CollectionLoader
import com.dashlane.item.v3.repositories.PasswordHealthRepository
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.db.SmartSpaceItemChecker
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KFunction1

class CredentialAsyncDataLoader @Inject constructor(
    private val collectionLoader: CollectionLoader<CredentialFormData>,
    private val passwordHealthRepository: PasswordHealthRepository,
    private val vaultDataQuery: VaultDataQuery,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val smartSpaceItemChecker: SmartSpaceItemChecker
) : AsyncDataLoader<CredentialFormData> {

    private val additionalAsyncData: MutableList<Deferred<Unit>> = mutableListOf()

    @Suppress("LongMethod")
    override fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<CredentialFormData>) -> Data<CredentialFormData>, Unit>,
        onAllDataLoaded: suspend () -> Unit
    ) {
        
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val canDelete = sharingPolicyDataProvider.isDeleteAllowed(
                    initialSummaryObject.id,
                    isNewItem,
                    initialSummaryObject.isShared
                )
                additionalDataLoadedFunction {
                    it.updateCommonData {
                        it.copy(canDelete = canDelete)
                    }
                }
            }
        )
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val sharingCount =
                    sharingPolicyDataProvider.getSharingCount(initialSummaryObject.id)
                additionalDataLoadedFunction {
                    it.updateCommonData {
                        it.copy(sharingCount = FormData.SharingCount(sharingCount))
                    }
                }
            }
        )
        additionalAsyncData.add(
            collectionLoader.loadAsync(scope, initialSummaryObject, additionalDataLoadedFunction)
        )
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val passwordHealth =
                    passwordHealthRepository.getPasswordHealth(itemId = initialSummaryObject.id)
                additionalDataLoadedFunction {
                    it.updateFormData { formData -> formData.copy(passwordHealth = passwordHealth) }
                }
            }
        )
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                additionalDataLoadedFunction {
                    it.updateFormData { formData -> formData.copy(otp = getOtp(initialSummaryObject.id)) }
                }
            }
        )
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val isForcedSpace = smartSpaceItemChecker.checkForceSpace(itemId = initialSummaryObject.id)
                additionalDataLoadedFunction {
                    it.updateCommonData { it.copy(isForcedSpace = isForcedSpace) }
                }
            }
        )
        scope.launch {
            additionalAsyncData.joinAll()
            onAllDataLoaded()
        }
    }

    override fun cancelAll() = additionalAsyncData.forEach { it.cancel() }

    private fun getOtp(itemId: String): Otp? {
        return vaultDataQuery.queryLegacy(vaultFilter { specificUid(itemId) })?.let {
            val syncObject = it.syncObject
            if (syncObject is SyncObject.Authentifiant) {
                return@let syncObject.otp()
            }
            return@let null
        }
    }
}