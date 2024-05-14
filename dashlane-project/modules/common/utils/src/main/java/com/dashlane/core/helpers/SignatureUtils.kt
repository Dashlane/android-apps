package com.dashlane.core.helpers

import com.dashlane.xml.domain.SyncObject

fun SyncObject.Authentifiant.LinkedServices.toAppSignature() = associatedAndroidApps
    ?.mapNotNull {
        it.packageName?.let { packageName ->
            AppSignature(
                packageName,
                it.sha256CertFingerprints,
                it.sha512CertFingerprints
            )
        }
    }

fun List<AppSignature>.toSyncObject() = SyncObject.Authentifiant.LinkedServices(
    this.map {
        SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps(
            SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource.USER,
            null,
            it.packageName,
            it.sha256Signatures,
            it.sha512Signatures
        )
    },
    listOf()
)