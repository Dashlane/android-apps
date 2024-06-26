package com.dashlane.autofill.linkedservices

import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.rememberaccount.model.RememberedFormSource
import com.dashlane.autofill.rememberaccount.services.FormSourceAuthentifiantLinker
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.valueWithoutWww
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject
import javax.inject.Named

class RememberToLinkedAppsMigration @Inject constructor(
    @Named("ApplicationLinkedPreference") private val applicationFormSourceAuthentifiantLinker: FormSourceAuthentifiantLinker,
    @Named("WebDomainLinkedPreference") private val webDomainFormSourceAuthentifiantLinker: FormSourceAuthentifiantLinker,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val dataSaver: DataSaver,
    private val vaultDataQuery: VaultDataQuery
) {
    @Suppress("UNCHECKED_CAST")
    suspend fun migrate() {
        getAllRemembered().forEach { rememberedFormSource ->
            vaultDataQuery.query(
                VaultFilter().apply {
                    ignoreUserLock()
                    specificDataType(SyncObjectType.AUTHENTIFIANT)
                    specificUid(rememberedFormSource.authentifiantId)
                }
            )?.let { vaultItem ->
                vaultItem as VaultItem<SyncObject.Authentifiant>
                val toSaveVaultItem = vaultItem.copySyncObject {
                    this.linkedServices = createAndMergeLinkedService(
                        rememberedFormSource.autoFillFormSource,
                        vaultItem.syncObject.linkedServices
                    )
                }.copyWithAttrs {
                    syncState = SyncState.MODIFIED
                }
                runCatching { dataSaver.save(toSaveVaultItem) }
                    .getOrElse { false }
                    .takeIf { it }
                    ?.let {
                        unlinkRemember(rememberedFormSource)
                    }
            }
        }
    }

    private suspend fun unlinkRemember(rememberedFormSource: RememberedFormSource) {
        when (rememberedFormSource.autoFillFormSource) {
            is ApplicationFormSource -> applicationFormSourceAuthentifiantLinker.unlink(
                rememberedFormSource.autoFillFormSource.packageName,
                rememberedFormSource.authentifiantId
            )
            is WebDomainFormSource -> webDomainFormSourceAuthentifiantLinker.unlink(
                rememberedFormSource.autoFillFormSource.webDomain,
                rememberedFormSource.authentifiantId
            )
        }
    }

    private suspend fun getAllRemembered(): List<RememberedFormSource> {
        val app = applicationFormSourceAuthentifiantLinker.allLinked()
            .map {
                RememberedFormSource(ApplicationFormSource(it.first), it.second)
            }

        val web = webDomainFormSourceAuthentifiantLinker.allLinked()
            .map {
                RememberedFormSource(WebDomainFormSource("", it.first), it.second)
            }
        return app + web
    }

    private fun createAndMergeLinkedService(
        autoFillFormSource: AutoFillFormSource,
        linkedServices: SyncObject.Authentifiant.LinkedServices?
    ): SyncObject.Authentifiant.LinkedServices {
        return when (autoFillFormSource) {
            is ApplicationFormSource -> {
                val newLinkedService = linkedServicesHelper
                    .getLinkedServicesWithAppSignature(autoFillFormSource.packageName)
                linkedServicesHelper.addLinkedApps(
                    linkedServices,
                    newLinkedService.associatedAndroidApps ?: emptyList()
                )
            }
            is WebDomainFormSource -> {
                val domainsToAdd = listOf(
                    SyncObject.Authentifiant.LinkedServices.AssociatedDomains(
                        autoFillFormSource.webDomain.toUrlDomainOrNull()?.valueWithoutWww(),
                        SyncObject.Authentifiant.LinkedServices.AssociatedDomains.Source.REMEMBER
                    )
                )
                linkedServicesHelper.addLinkedDomains(linkedServices, domainsToAdd)
            }
        }
    }
}