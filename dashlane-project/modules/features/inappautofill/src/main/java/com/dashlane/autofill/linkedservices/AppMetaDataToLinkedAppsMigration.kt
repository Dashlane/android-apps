package com.dashlane.autofill.linkedservices

import android.content.pm.PackageManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.util.getAppName
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class AppMetaDataToLinkedAppsMigration @Inject constructor(
    private val packageManager: PackageManager,
    private val dataSaver: DataSaver,
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery
) {
    @Suppress("UNCHECKED_CAST")
    suspend fun migrate() {
        genericDataQuery.queryAll(
            GenericFilter().apply {
                specificDataType(SyncObjectType.AUTHENTIFIANT)
            }
        ).forEach { summary ->
            
            val linkedApps = (summary as SummaryObject.Authentifiant).appMetaData?.let { appMetaData ->
                getLinkedAppsFromAppMetaData(appMetaData)
            }

            
            checkAppToMigrate(linkedApps, summary)
        }
    }

    private suspend fun checkAppToMigrate(
        linkedApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>?,
        summary: SummaryObject.Authentifiant
    ) {
        if (!linkedApps.isNullOrEmpty()) {
            vaultDataQuery.query(
                VaultFilter().apply {
                    ignoreUserLock()
                    specificDataType(SyncObjectType.AUTHENTIFIANT)
                    specificUid(summary.id)
                }
            )?.let { vaultItem ->
                vaultItem as VaultItem<SyncObject.Authentifiant>
                val toSaveVaultItem = getVaultItemToSave(vaultItem, linkedApps)
                runCatching { dataSaver.save(toSaveVaultItem) }.getOrElse { false }.let {
                }
            }
        }
    }

    fun getVaultItemToSave(
        vaultItem: VaultItem<SyncObject.Authentifiant>,
        linkedApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>
    ): VaultItem<SyncObject.Authentifiant> {
        return vaultItem
            .copySyncObject {
                this.linkedServices = SyncObject.Authentifiant.LinkedServices(
                    mergeLinkedApps(linkedServices?.associatedAndroidApps, linkedApps),
                    this.linkedServices?.associatedDomains
                )
                this.appMetaData = null
            }
            .copyWithAttrs {
                syncState = SyncState.MODIFIED
            }
    }

    fun getLinkedAppsFromAppMetaData(appMetaData: SummaryObject.AppMetaData): List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps> {
        return appMetaData.androidLinkedApplications?.map { linkedApp ->
            val name = linkedApp.packageName?.let {
                packageManager.getAppName(it)
            }
            SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps(
                SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource.DASHLANE,
                name,
                linkedApp.packageName,
                linkedApp.sha256CertFingerprints,
                linkedApp.sha512CertFingerprints
            )
        } ?: emptyList()
    }

    private fun mergeLinkedApps(
        originalLinkedApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>?,
        toMergeLinkedApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>
    ): List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps> {
        val mergedLinkedApps = mutableListOf<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>()

        
        originalLinkedApps?.forEach { original ->
            val newValue = toMergeLinkedApps.firstOrNull { it.packageName == original.packageName }
            if (newValue != null) {
                mergedLinkedApps.add(
                    
                    SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps(
                        original.linkSource,
                        newValue.name,
                        original.packageName,
                        original.sha256CertFingerprints,
                        original.sha512CertFingerprints
                    )
                )
            } else {
                mergedLinkedApps.add(original)
            }
        }

        
        toMergeLinkedApps
            .filter { toMerge ->
                originalLinkedApps == null || !originalLinkedApps.any { toMerge.packageName == it.packageName }
            }
            .forEach {
                mergedLinkedApps.add(it)
            }
        return mergedLinkedApps
    }

    companion object {
        private const val TAG = "AppMetaDataToLinkedAppsMigration"
    }
}
