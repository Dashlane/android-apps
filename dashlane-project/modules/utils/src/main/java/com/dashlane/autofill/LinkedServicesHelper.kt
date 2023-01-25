package com.dashlane.autofill

import android.content.Context
import android.content.pm.PackageManager
import com.dashlane.core.helpers.AppSignature
import com.dashlane.util.PackageUtilities
import com.dashlane.util.getAppName
import com.dashlane.util.matchDomain
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource
import com.dashlane.xml.domain.SyncObject.Authentifiant.LinkedServices.AssociatedDomains.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkedServicesHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManager: PackageManager
) {

    fun getLinkedServicesWithAppSignature(
        packageName: String?,
        linkSource: LinkSource = LinkSource.USER
    ): SyncObject.Authentifiant.LinkedServices {
        val appSignatures = listOfNotNull(PackageUtilities.getSignatures(context, packageName))
        return addSignatureToLinkedServices(appSignatures, null, linkSource)
    }

    

    fun addSignatureToLinkedServices(
        signatures: List<AppSignature>,
        originalLinkedServices: SyncObject.Authentifiant.LinkedServices?,
        linkSource: LinkSource = LinkSource.USER
    ): SyncObject.Authentifiant.LinkedServices {
        val originalNotNull = SyncObject.Authentifiant.LinkedServices(
            originalLinkedServices?.associatedAndroidApps ?: listOf(),
            originalLinkedServices?.associatedDomains ?: listOf()
        )
        val linkedApps = signatures.mapTo(mutableSetOf()) { signature ->
            originalNotNull.associatedAndroidApps
                ?.firstOrNull { it.packageName == signature.packageName }
                .let { original ->
                    mergeAssociatedAppSignature(original, signature, linkSource)
                }
        }

        
        originalLinkedServices?.associatedAndroidApps
            ?.filter { !linkedApps.any { linkedApp -> linkedApp.packageName == it.packageName } }
            ?.forEach {
                linkedApps.add(it)
            }
        return SyncObject.Authentifiant.LinkedServices(linkedApps.toList(), originalNotNull.associatedDomains)
    }

    private fun mergeAssociatedAppSignature(
        original: SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps?,
        signature: AppSignature,
        linkSource: LinkSource
    ): SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps {
        val sha256Signatures = mergeSets(
            original?.sha256CertFingerprints?.toSet(),
            signature.sha256Signatures?.toSet()
        ).toSortedSet()
        val sha512Signatures = mergeSets(
            original?.sha512CertFingerprints?.toSet(),
            signature.sha512Signatures?.toSet()
        ).toSortedSet()
        val appName = packageManager.getAppName(signature.packageName)
        return SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps(
            linkSource = linkSource,
            name = appName ?: original?.name,
            packageName = signature.packageName,
            sha256CertFingerprints = sha256Signatures.toList(),
            sha512CertFingerprints = sha512Signatures.toList()
        )
    }

    private fun <T> mergeSets(first: Set<T>?, second: Set<T>?): Set<T> = first.orEmpty() + second.orEmpty()

    fun replaceAllLinkedAppsByUser(
        linkedServices: SyncObject.Authentifiant.LinkedServices?,
        apps: List<String>
    ): SyncObject.Authentifiant.LinkedServices {
        val existingLinkedApps = linkedServices?.associatedAndroidApps
            ?.filter { associated -> apps.any { it == associated.packageName } || associated.linkSource == LinkSource.DASHLANE }
        val newLinkedServices = SyncObject.Authentifiant.LinkedServices(
            existingLinkedApps, linkedServices?.associatedDomains
        )
        return addLinkedApps(
            newLinkedServices,
            apps.mapNotNull {
                getLinkedServicesWithAppSignature(it).associatedAndroidApps
            }.flatten()
        )
    }

    

    fun addLinkedApps(
        linkedServices: SyncObject.Authentifiant.LinkedServices?,
        toAddLinkedApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>,
    ): SyncObject.Authentifiant.LinkedServices {
        val mergedAndNewApps = toAddLinkedApps
            .map { newAssociatedApp ->
                val existingValue = linkedServices?.associatedAndroidApps
                    ?.firstOrNull { it.packageName == newAssociatedApp.packageName }
                if (existingValue != null) {
                    SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps(
                        existingValue.linkSource,
                        newAssociatedApp.name,
                        newAssociatedApp.packageName,
                        newAssociatedApp.sha256CertFingerprints,
                        newAssociatedApp.sha512CertFingerprints
                    )
                } else {
                    newAssociatedApp
                }
            }
        val oldApps = linkedServices?.associatedAndroidApps
            ?.filterNot { mergedAndNewApps.any { existing -> it.packageName == existing.packageName } }
            ?: emptyList()
        return SyncObject.Authentifiant.LinkedServices(
            oldApps + mergedAndNewApps,
            linkedServices?.associatedDomains ?: emptyList()
        )
    }

    fun replaceAllLinkedDomains(
        linkedServices: SyncObject.Authentifiant.LinkedServices?,
        domains: List<String>,
    ): SyncObject.Authentifiant.LinkedServices {
        val existingLinkedWebsites = linkedServices?.associatedDomains
            ?.filter { associated -> domains.any { it == associated.domain } }
        val newLinkedServices = SyncObject.Authentifiant.LinkedServices(
            linkedServices?.associatedAndroidApps, existingLinkedWebsites
        )
        return addLinkedDomains(
            newLinkedServices,
            domains.map {
                SyncObject.Authentifiant.LinkedServices.AssociatedDomains(it, Source.MANUAL)
            }
        )
    }

    

    fun addLinkedDomains(
        linkedServices: SyncObject.Authentifiant.LinkedServices?,
        toAddAssociatedDomains: List<SyncObject.Authentifiant.LinkedServices.AssociatedDomains>,
    ): SyncObject.Authentifiant.LinkedServices {
        val newDomains = toAddAssociatedDomains.mapNotNull { newAssociatedDomain ->
            val existingValue = linkedServices?.associatedDomains
                ?.firstOrNull { it.domain.matchDomain(newAssociatedDomain.domain) }
            if (existingValue == null) newAssociatedDomain else null
        }
        val oldDomains = linkedServices?.associatedDomains
            ?.filterNot { newDomains.any { existing -> it.domain.matchDomain(existing.domain) } }
            ?: emptyList()
        return SyncObject.Authentifiant.LinkedServices(
            linkedServices?.associatedAndroidApps ?: emptyList(),
            oldDomains + newDomains
        )
    }
}