package com.dashlane.core.helpers

import android.content.Context
import android.content.pm.PackageManager
import com.dashlane.core.helpers.SignatureVerification.KnownApplicationIncorrect
import com.dashlane.core.helpers.SignatureVerification.KnownApplicationVerified
import com.dashlane.core.helpers.SignatureVerification.MismatchUnknown
import com.dashlane.core.helpers.SignatureVerification.NoSignatureUnknown
import com.dashlane.core.helpers.SignatureVerification.VaultLinkedApps
import com.dashlane.core.helpers.SignatureVerification.VaultLinkedAppsIncorrect
import com.dashlane.core.helpers.SignatureVerification.VaultLinkedAppsVerified
import com.dashlane.core.helpers.SignatureVerification.WithSignatureUnknown
import com.dashlane.ext.application.KnownApplication
import com.dashlane.ext.application.KnownApplication.Companion.getKnownApplication
import com.dashlane.ext.application.KnownApplication.Companion.getSignature
import com.dashlane.util.PackageUtilities.getSignatures
import com.dashlane.vault.summary.SummaryObject
import java.util.Locale



class PackageNameSignatureHelper {

    

    fun getPackageNameSignatureVerificationStatus(
        context: Context?,
        packageName: String,
        linkedServices: SummaryObject.LinkedServices?,
        url: String?
    ): PackageSignatureStatus =
        getPackageNameSignatureVerificationStatus(context?.packageManager, packageName, linkedServices, url)

    fun getPackageNameSignatureVerificationStatus(
        packageManager: PackageManager?,
        packageName: String,
        linkedServices: SummaryObject.LinkedServices?,
        url: String?
    ): PackageSignatureStatus =
        getPackageNameDetailedSignatureVerificationStatus(packageManager, packageName, linkedServices, url).secureResult

    

    fun getPackageNameDetailedSignatureVerificationStatus(
        context: Context?,
        packageName: String,
        linkedServices: SummaryObject.LinkedServices?,
        url: String?
    ): SignatureVerification {
        val packageManager = context?.packageManager
        return getPackageNameDetailedSignatureVerificationStatus(packageManager, packageName, linkedServices, url)
    }

    

    private fun getPackageNameDetailedSignatureVerificationStatus(
        packageManager: PackageManager?,
        packageName: String,
        linkedServices: SummaryObject.LinkedServices?,
        url: String?
    ): SignatureVerification {
        val apkSignatures = getSignatures(packageManager, packageName)
        if (apkSignatures == null || !apkSignatures.hasSignatures()) {
            return NoSignatureUnknown()
        }
        val linkedServicesStoredSignatures = getStoredSignatures(packageName, linkedServices)
        if (linkedServicesStoredSignatures.hasSignatures()) {
            return signatureVerification(apkSignatures, linkedServicesStoredSignatures)
        }

        
        val knownApplication = getKnownApplication(packageName) ?: return WithSignatureUnknown(apkSignatures)
        val knownApplicationStoredSignature = knownApplication.signatures
        if (knownApplicationStoredSignature == null || !knownApplicationStoredSignature.hasSignatures()) {
            return WithSignatureUnknown(apkSignatures)
        }
        val knownApplicationForUrlStoredSignature = getSignature(packageName, url)
        if (knownApplicationForUrlStoredSignature != null && knownApplicationForUrlStoredSignature.hasSignatures()) {
            
            return knownSignatureVerification(apkSignatures, knownApplicationForUrlStoredSignature, knownApplication)
        }
        return if (isSignaturesMatch(apkSignatures, knownApplicationStoredSignature)) {
            
            MismatchUnknown(apkSignatures, knownApplication)
        } else {
            KnownApplicationIncorrect(apkSignatures, knownApplicationStoredSignature, knownApplication)
        }
    }

    private fun signatureVerification(apkSignatures: AppSignature, appSignature: AppSignature): VaultLinkedApps {
        return if (isSignaturesMatch(apkSignatures, appSignature)) {
            VaultLinkedAppsVerified(apkSignatures, appSignature)
        } else {
            VaultLinkedAppsIncorrect(apkSignatures, appSignature)
        }
    }

    private fun knownSignatureVerification(
        apkSignatures: AppSignature?,
        knownApplicationSignature: AppSignature?,
        knownApplication: KnownApplication
    ): SignatureVerification.Known {
        return if (isSignaturesMatch(apkSignatures!!, knownApplicationSignature!!)) {
            KnownApplicationVerified(apkSignatures, knownApplicationSignature, knownApplication)
        } else {
            KnownApplicationIncorrect(apkSignatures, knownApplicationSignature, knownApplication)
        }
    }

    

    private fun isSignaturesMatch(apkSignatures: AppSignature, storedSignatures: AppSignature): Boolean {
        return if (storedSignatures.sha256Signatures.isNullOrEmpty()) {
            isAllSignaturesFound(apkSignatures.sha512Signatures, storedSignatures.sha512Signatures)
        } else {
            isAllSignaturesFound(apkSignatures.sha256Signatures, storedSignatures.sha256Signatures)
        }
    }

    

    private fun isAllSignaturesFound(
        apkSignatures: List<String>?,
        storedSignatures: List<String>?
    ): Boolean {
        if (apkSignatures.isNullOrEmpty()) {
            return storedSignatures.isNullOrEmpty()
        }
        if (storedSignatures.isNullOrEmpty()) {
            return false
        }
        apkSignatures.forEach { apkSignature ->
            if (!storedSignatures.any { sameSignature(apkSignature, it) }) {
                return false
            }
        }
        return true
    }

    

    private fun getStoredSignatures(
        packageName: String,
        linkedServices: SummaryObject.LinkedServices?,
    ): AppSignature {
        linkedServices?.associatedAndroidApps?.firstOrNull { it.packageName == packageName }.let {
            return AppSignature(packageName, it?.sha256CertFingerprints, it?.sha512CertFingerprints)
        }
    }

    private fun sameSignature(signature1: String?, signature2: String?): Boolean {
        return signature1 != null && signature2 != null &&
                (signature1 == signature2 || getNormalizedSha(signature1) == getNormalizedSha(signature2))
    }

    private fun getNormalizedSha(sha: String): String {
        return sha.replace(":", "").lowercase(Locale.ENGLISH)
    }

    companion object {
        const val KEY_PACKAGE_NAME = "package_name"
    }
}