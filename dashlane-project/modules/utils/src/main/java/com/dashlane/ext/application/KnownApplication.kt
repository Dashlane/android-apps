package com.dashlane.ext.application

import com.dashlane.core.helpers.AppSignature
import com.dashlane.url.UrlDomain
import com.dashlane.url.findAllChildrenOf
import com.dashlane.url.toUrlDomainOrNull

interface KnownApplication {
    val packageName: String

    val signatures: AppSignature?

    val mainDomain: String?

    fun isSecureToUse(url: String?): Boolean

    fun canOpen(url: String?): Boolean

    data class App(
        override val packageName: String,
        override val signatures: AppSignature?,
        val mainUrlDomain: UrlDomain,
        private val linkedDomainsId: String? = mainUrlDomain.let {
            KnownLinkedDomains.getMatchingLinkedDomainId(mainUrlDomain)
        },
        val keywords: Set<String>?,
        val allowedDomains: Set<UrlDomain>?
    ) : KnownApplication {
        override val mainDomain: String = mainUrlDomain.value

        override fun isSecureToUse(url: String?): Boolean {
            val urlDomainToAccept =
                url?.toUrlDomainOrNull() ?: return false 

            return matchMainDomain(
                mainUrlDomain,
                urlDomainToAccept
            ) || matchCommonUsedUrlDomains(urlDomainToAccept) || matchLinkedDomains(urlDomainToAccept)
        }

        private fun matchLinkedDomains(urlDomainToAccept: UrlDomain): Boolean {
            return linkedDomainsId?.let {
                KnownLinkedDomains.getMatchingLinkedDomainId(urlDomainToAccept) == it
            } ?: false
        }

        private fun matchCommonUsedUrlDomains(urlDomainToAccept: UrlDomain): Boolean =
            allowedDomains?.findAllChildrenOf(urlDomainToAccept)?.isNotEmpty() ?: false

        private fun matchMainDomain(knownAppMainDomain: UrlDomain, urlDomainToAccept: UrlDomain): Boolean =
            knownAppMainDomain.root == urlDomainToAccept.root

        override fun canOpen(url: String?): Boolean {
            val urlDomainToAccept =
                url?.toUrlDomainOrNull() ?: return false 

            return matchMainDomain(mainUrlDomain, urlDomainToAccept) || matchLinkedDomains(urlDomainToAccept)
        }
    }

    
    sealed class Signature {

        abstract fun toAppSignature(packageName: String): AppSignature?

        class Sha256(val signatures: Array<out String>) : Signature() {
            override fun toAppSignature(packageName: String) =
                AppSignature(packageName, sha256Signatures = signatures.toList())
        }

        class Sha512(val signatures: Array<out String>) : Signature() {
            override fun toAppSignature(packageName: String) =
                AppSignature(packageName, sha512Signatures = signatures.toList())
        }
    }

    companion object {
        fun sha256(vararg signatures: String) = Signature.Sha256(signatures)
        fun sha512(vararg signatures: String) = Signature.Sha512(signatures)
    }
}