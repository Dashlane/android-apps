package com.dashlane.item.v3.loaders

import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.otp
import com.dashlane.item.v3.ItemEditFragmentArgs
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.loaders.common.CanDeleteLoader
import com.dashlane.item.v3.loaders.common.CollectionLoader
import com.dashlane.item.v3.loaders.common.SharingCountLoader
import com.dashlane.item.v3.repositories.PasswordHealthRepository
import com.dashlane.item.v3.viewmodels.Data
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
    override val collectionLoader: CollectionLoader<CredentialFormData>,
    private val canDeleteLoader: CanDeleteLoader<CredentialFormData>,
    private val sharingCountLoader: SharingCountLoader<CredentialFormData>,
    private val passwordHealthRepository: PasswordHealthRepository,
    private val vaultDataQuery: VaultDataQuery,
    private val smartSpaceItemChecker: SmartSpaceItemChecker,
) : AsyncDataLoader<CredentialFormData> {
    private val additionalAsyncData: MutableList<Deferred<Unit>> = mutableListOf()

    @Suppress("LongMethod")
    override fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        navArgs: ItemEditFragmentArgs,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<CredentialFormData>) -> Data<CredentialFormData>, Unit>,
        onAllDataLoaded: suspend () -> Unit,
    ) {
        
        additionalAsyncData.add(canDeleteLoader.loadAsync(scope, initialSummaryObject, isNewItem, additionalDataLoadedFunction))
        additionalAsyncData.add(sharingCountLoader.loadAsync(scope, initialSummaryObject, additionalDataLoadedFunction))
        additionalAsyncData.add(
            collectionLoader.loadAsync(scope, initialSummaryObject, additionalDataLoadedFunction),
        )
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val passwordHealth =
                    passwordHealthRepository.getPasswordHealth(itemId = initialSummaryObject.id)
                additionalDataLoadedFunction {
                    it.updateFormData { formData -> formData.copy(passwordHealth = passwordHealth) }
                }
            },
        )
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val otp = getOtp(initialSummaryObject.id) ?: navArgs.otp
                additionalDataLoadedFunction {
                    it.updateFormData { formData -> formData.copy(otp = otp) }
                }
            },
        )
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val isForcedSpace = smartSpaceItemChecker.checkForceSpace(initialSummaryObject)
                additionalDataLoadedFunction {
                    it.updateCommonData { it.copy(isForcedSpace = isForcedSpace) }
                }
            },
        )
        scope.launch {
            additionalAsyncData.joinAll()
            onAllDataLoaded()
        }
    }

    override fun cancelAll() = additionalAsyncData.forEach { it.cancel() }

    private suspend fun getOtp(itemId: String): Otp? {
        return vaultDataQuery.query(vaultFilter { specificUid(itemId) })?.let {
            val syncObject = it.syncObject
            if (syncObject is SyncObject.Authentifiant) {
                return@let syncObject.otp()
            }
            return@let null
        }
    }
}