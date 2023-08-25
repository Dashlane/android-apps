package com.dashlane.core.helpers

import com.dashlane.ext.application.KnownApplication

interface SignatureVerification {
    val secureResult: PackageSignatureStatus
    val signatureInstalled: AppSignature?
    val signatureKnown: AppSignature?
    val signatureInMeta: AppSignature?
    val knownApplication: KnownApplication?

    fun isUnknown(): Boolean = secureResult == PackageSignatureStatus.UNKNOWN
    fun isIncorrect(): Boolean = secureResult == PackageSignatureStatus.INCORRECT
    fun isVerified(): Boolean = secureResult == PackageSignatureStatus.VERIFIED

    abstract class Incorrect : SignatureVerification {
        override val secureResult: PackageSignatureStatus = PackageSignatureStatus.INCORRECT
        abstract override val signatureInstalled: AppSignature
    }

    abstract class Verified : SignatureVerification {
        override val secureResult: PackageSignatureStatus = PackageSignatureStatus.VERIFIED
        abstract override val signatureInstalled: AppSignature
    }

    abstract class Unknown : SignatureVerification {
        override val secureResult: PackageSignatureStatus = PackageSignatureStatus.UNKNOWN
        override val signatureKnown: AppSignature? = null
        override val signatureInMeta: AppSignature? = null
    }

    abstract class UnknownWithSignature : Unknown() {
        abstract override val signatureInstalled: AppSignature
    }

    interface Known : SignatureVerification {
        override val signatureInstalled: AppSignature
        override val signatureKnown: AppSignature
        override val knownApplication: KnownApplication
    }

    interface VaultLinkedApps : SignatureVerification {
        override val signatureInstalled: AppSignature
        override val signatureInMeta: AppSignature
    }

    class KnownApplicationIncorrect(
        override val signatureInstalled: AppSignature,
        override val signatureKnown: AppSignature,
        override val knownApplication: KnownApplication
    ) : Known, Incorrect() {
        override val signatureInMeta: AppSignature? = null
    }

    class VaultLinkedAppsIncorrect(
        override val signatureInstalled: AppSignature,
        override val signatureInMeta: AppSignature
    ) : VaultLinkedApps, Incorrect() {
        override val signatureKnown: AppSignature? = null
        override val knownApplication: KnownApplication? = null
    }

    class KnownApplicationVerified(
        override val signatureInstalled: AppSignature,
        override val signatureKnown: AppSignature,
        override val knownApplication: KnownApplication
    ) : Known, Verified() {
        override val signatureInMeta: AppSignature? = null
    }

    class VaultLinkedAppsVerified(
        override val signatureInstalled: AppSignature,
        override val signatureInMeta: AppSignature
    ) : VaultLinkedApps, Verified() {
        override val signatureKnown: AppSignature? = null
        override val knownApplication: KnownApplication? = null
    }

    class NoSignatureUnknown : Unknown() {
        override val signatureInstalled: AppSignature? = null
        override val knownApplication: KnownApplication? = null
    }

    class WithSignatureUnknown(
        override val signatureInstalled: AppSignature
    ) : UnknownWithSignature() {
        override val knownApplication: KnownApplication? = null
    }

    class MismatchUnknown(
        override val signatureInstalled: AppSignature,
        override val knownApplication: KnownApplication
    ) : UnknownWithSignature()
}
