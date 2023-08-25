package com.dashlane.nitro

import com.dashlane.nitro.AttestationDocument.Companion.decodeAttestationDocumentOrNull
import com.dashlane.nitro.cryptography.SecretStreamServerInfo
import com.dashlane.nitro.util.ECDSASignatureConverter
import com.dashlane.nitro.util.encodeHex
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import java.io.ByteArrayInputStream
import java.security.Signature
import java.security.cert.CertPathValidator
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import javax.inject.Inject

internal class AttestationValidatorImpl @Inject constructor() : AttestationValidator {
    override fun validate(attestation: CoseSign1): SecretStreamServerInfo {
        val attestationDocument = attestation.payload.decodeAttestationDocumentOrNull()
            ?: throw NitroException()

        if (!validateCertificateChain(attestationDocument)) {
            throw NitroException("Invalid certificate chain")
        }

        if (!verifySignature(attestationDocument, attestation)) {
            throw NitroException("Invalid signature")
        }

        if (!verifyPcr(attestationDocument)) {
            throw NitroException("Cannot match PCRs")
        }
        return SecretStreamServerInfo(
            publicKey = attestationDocument.userData.publicKey,
            header = attestationDocument.userData.header
        )
    }

    private fun validateCertificateChain(document: AttestationDocument): Boolean {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val rootCertificate =
            certificateFactory.generateCertificate(AWS_NITRO_ROOT_CERT.byteInputStream()) as X509Certificate
        val certificateChain = buildList {
            add(document.certificate)
            addAll(document.cabundle.reversed())
        }.map { certificateFactory.generateCertificate(ByteArrayInputStream(it)) }
        val certPath = certificateFactory.generateCertPath(certificateChain)
        val trustAnchor = TrustAnchor(rootCertificate, null)
        val validationParameters = PKIXParameters(setOf(trustAnchor))
        validationParameters.isRevocationEnabled = false

        return try {
            CertPathValidator.getInstance("PKIX").validate(certPath, validationParameters)
            
            true
        } catch (e: CertPathValidatorException) {
            false
        }
    }

    private fun verifySignature(
        document: AttestationDocument,
        coseSign1: CoseSign1
    ): Boolean {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate =
            certificateFactory.generateCertificate(document.certificate.inputStream()) as X509Certificate

        val signedPayload = CBORMapper().writeValueAsBytes(
            arrayOf(
                "Signature1",
                coseSign1.protectedHeader,
                ByteArray(0),
                coseSign1.payload
            )
        )

        val signature = Signature.getInstance("SHA384withECDSA").apply {
            initVerify(certificate.publicKey)
            update(signedPayload)
        }
        val convertedSignature = try {
            ECDSASignatureConverter.convertConcatToDerFormat(coseSign1.signature)
        } catch (e: ECDSASignatureConverter.CoseException) {
            return false
        }

        return signature.verify(convertedSignature)
    }

    private fun verifyPcr(attestationDocument: AttestationDocument): Boolean {
        val pcrs = attestationDocument.pcrs
        if (pcrs.size() < 9) return false

        val pcr3Check = PCR3 == pcrs["3"].binaryValue().encodeHex()
        val pcr8Check = if (PCR8.isNotEmpty()) {
            PCR8 == pcrs["8"].binaryValue().encodeHex()
        } else {
            
            true
        }
        return pcr3Check && pcr8Check
    }

    companion object {
        private const val AWS_NITRO_ROOT_CERT = """-----BEGIN CERTIFICATE-----
MIICETCCAZagAwIBAgIRAPkxdWgbkK/hHUbMtOTn+FYwCgYIKoZIzj0EAwMwSTEL
MAkGA1UEBhMCVVMxDzANBgNVBAoMBkFtYXpvbjEMMAoGA1UECwwDQVdTMRswGQYD
VQQDDBJhd3Mubml0cm8tZW5jbGF2ZXMwHhcNMTkxMDI4MTMyODA1WhcNNDkxMDI4
MTQyODA1WjBJMQswCQYDVQQGEwJVUzEPMA0GA1UECgwGQW1hem9uMQwwCgYDVQQL
DANBV1MxGzAZBgNVBAMMEmF3cy5uaXRyby1lbmNsYXZlczB2MBAGByqGSM49AgEG
BSuBBAAiA2IABPwCVOumCMHzaHDimtqQvkY4MpJzbolL
48C8WBoyt7F2Bw7eEtaaP+ohG2bnUs990d0JX28TcPQXCEPZ3BABIeTPYwEoCWZE
h8l5YoQwTcU/9KNCMEAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUkCW1DdkF
R+eWw5b6cp3PmanfS5YwDgYDVR0PAQH/BAQDAgGGMAoGCCqGSM49BAMDA2kAMGYC
MQCjfy+Rocm9Xue4YnwWmNJVA44fA0P5W2OpYow9OYCVRaEevL8uO1XYru5xtMPW
rfMCMQCi85sWBbJwKKXdS6BptQFuZbT73o/gBh1qUxl/nNr12UO8Yfwr6wPLb+6N
IwLz3/Y=
-----END CERTIFICATE-----"""

        private const val PCR3: String = BuildConfig.NITRO_PCR3
        private const val PCR8: String = BuildConfig.NITRO_PCR8
    }
}
