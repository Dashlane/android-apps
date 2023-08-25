package com.dashlane.authenticator

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.Action.EDIT
import com.dashlane.hermes.generated.definitions.Button.SEE_ALL
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.EncryptionAlgorithm
import com.dashlane.hermes.generated.definitions.Field.OTP_SECRET
import com.dashlane.hermes.generated.definitions.FlowStep.COMPLETE
import com.dashlane.hermes.generated.definitions.FlowStep.ERROR
import com.dashlane.hermes.generated.definitions.FlowStep.START
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType.CREDENTIAL
import com.dashlane.hermes.generated.definitions.OtpAdditionError
import com.dashlane.hermes.generated.definitions.OtpAdditionError.MISSING_LOGIN
import com.dashlane.hermes.generated.definitions.OtpAdditionError.NON_OTP_QR_CODE
import com.dashlane.hermes.generated.definitions.OtpAdditionError.NON_OTP_TEXT_CODE
import com.dashlane.hermes.generated.definitions.OtpAdditionError.UNKNOWN_ERROR
import com.dashlane.hermes.generated.definitions.OtpAdditionMode.QR_CODE
import com.dashlane.hermes.generated.definitions.OtpAdditionMode.TEXT_CODE
import com.dashlane.hermes.generated.definitions.OtpSpecifications
import com.dashlane.hermes.generated.definitions.OtpType.HOTP
import com.dashlane.hermes.generated.definitions.OtpType.TOTP
import com.dashlane.hermes.generated.definitions.Space.PERSONAL
import com.dashlane.hermes.generated.definitions.Space.PROFESSIONAL
import com.dashlane.hermes.generated.events.anonymous.AddTwoFactorAuthenticationToCredentialAnonymous
import com.dashlane.hermes.generated.events.anonymous.CopyVaultItemFieldAnonymous
import com.dashlane.hermes.generated.events.anonymous.RemoveTwoFactorAuthenticationFromCredentialAnonymous
import com.dashlane.hermes.generated.events.anonymous.UpdateCredentialAnonymous
import com.dashlane.hermes.generated.events.user.AddTwoFactorAuthenticationToCredential
import com.dashlane.hermes.generated.events.user.Click
import com.dashlane.hermes.generated.events.user.CopyVaultItemField
import com.dashlane.hermes.generated.events.user.UpdateVaultItem
import javax.inject.Inject

class AuthenticatorLogger @Inject constructor(private val hermesLogRepository: LogRepository) {

    private var professional: Boolean? = null

    private var domain: Domain? = null

    fun setup(professional: Boolean, domain: Domain?): AuthenticatorLogger {
        this.domain = domain
        this.professional = professional
        return this
    }

    fun logClickSeeAll() = hermesLogRepository.queueEvent(Click(SEE_ALL))

    fun logCopyOtpCode(itemId: String, domain: String?) {
        hermesLogRepository.queueEvent(
            CopyVaultItemField(
                field = OTP_SECRET,
                itemType = CREDENTIAL,
                itemId = ItemId(itemId),
                isProtected = false
            )
        )
        domain?.let {
            hermesLogRepository.queueEvent(
                CopyVaultItemFieldAnonymous(
                    field = OTP_SECRET,
                    itemType = CREDENTIAL,
                    domain = Domain(Sha256Hash.of(domain), DomainType.WEB)
                )
            )
        }
    }

    fun logUpdateCredential(itemId: String) {
        val space = getSpace() ?: PERSONAL
        hermesLogRepository.queueEvent(
            UpdateVaultItem(
                action = EDIT,
                fieldsEdited = listOf(OTP_SECRET),
                itemId = ItemId(id = itemId),
                itemType = CREDENTIAL,
                space = space
            )
        )
        hermesLogRepository.queueEvent(
            UpdateCredentialAnonymous(
                action = EDIT,
                domain = domain ?: Domain(null, DomainType.WEB),
                fieldList = listOf(OTP_SECRET),
                space = space
            )
        )
    }

    fun logStartAdd2fa(itemId: String?, byScan: Boolean) {
        val additionMode = getAdditionMode(byScan)
        hermesLogRepository.queueEvent(
            AddTwoFactorAuthenticationToCredential(
                flowStep = START,
                itemId = itemId.getItemId(),
                otpAdditionMode = additionMode,
                space = getSpace()
            )
        )
    }

    fun logCompleteAdd2fa(itemId: String?, otp: Otp) {
        val additionMode = otp.getAdditionMode()
        hermesLogRepository.queueEvent(
            AddTwoFactorAuthenticationToCredential(
                flowStep = COMPLETE,
                itemId = itemId.getItemId(),
                otpAdditionMode = additionMode,
                space = getSpace()
            )
        )
        hermesLogRepository.queueEvent(
            AddTwoFactorAuthenticationToCredentialAnonymous(
                otpSpecifications = createOtpSpecifications(otp),
                flowStep = COMPLETE,
                domain = domain,
                otpAdditionMode = additionMode,
                space = getSpace(),
                authenticatorIssuerId = otp.issuer?.let { Sha256Hash.of(it) }
            )
        )
    }

    fun logPinError(itemId: String?, otp: Otp) {
        val pinError = otp.getPinError()
        logError(otp, pinError, false, itemId)
    }

    fun logMissingLoginError(otp: Otp) = logError(otp, MISSING_LOGIN, true)

    fun logUnknownError(itemId: String?, otp: Otp?, byScan: Boolean) =
        logError(otp, UNKNOWN_ERROR, byScan, itemId)

    private fun logError(
        otp: Otp?,
        error: OtpAdditionError,
        byScan: Boolean,
        itemId: String? = null
    ) {
        val additionMode = otp?.getAdditionMode() ?: getAdditionMode(byScan)
        hermesLogRepository.queueEvent(
            AddTwoFactorAuthenticationToCredential(
                flowStep = ERROR,
                itemId = itemId.getItemId(),
                otpAdditionMode = additionMode,
                otpAdditionError = error,
                space = getSpace()
            )
        )
        hermesLogRepository.queueEvent(
            AddTwoFactorAuthenticationToCredentialAnonymous(
                otpSpecifications = createOtpSpecifications(otp),
                flowStep = ERROR,
                otpAdditionMode = additionMode,
                otpAdditionError = error,
                space = getSpace()
            )
        )
    }

    fun logRemove2fa(issuer: String?) = hermesLogRepository.queueEvent(
        RemoveTwoFactorAuthenticationFromCredentialAnonymous(
            domain = domain!!,
            space = getSpace(professional)!!,
            authenticatorIssuerId = issuer?.let { Sha256Hash.of(it) }
        )
    )

    private fun Otp.getAdditionMode() = if (url != null) QR_CODE else TEXT_CODE

    private fun Otp.getPinError() = if (url != null) NON_OTP_QR_CODE else NON_OTP_TEXT_CODE

    private fun getAdditionMode(byScan: Boolean) = if (byScan) QR_CODE else TEXT_CODE

    private fun getSpace(professional: Boolean? = this.professional) = when (professional) {
        true -> PROFESSIONAL
        false -> PERSONAL
        else -> null
    }

    private fun String?.getItemId() = this?.let { ItemId(this) }

    private fun createOtpSpecifications(otp: Otp?): OtpSpecifications? {
        otp ?: return null
        val algorithm =
            EncryptionAlgorithm.values().firstOrNull { otp.algorithm.lowercase() == it.code }
                ?: EncryptionAlgorithm.OTHER
        return when (otp) {
            is Totp -> OtpSpecifications(
                durationOtpValidity = otp.period,
                encryptionAlgorithm = algorithm,
                otpType = TOTP,
                otpCodeSize = otp.digits
            )
            is Hotp -> OtpSpecifications(
                encryptionAlgorithm = algorithm,
                otpType = HOTP,
                otpIncrementCount = otp.counter.toInt(),
                otpCodeSize = otp.digits
            )
            else -> null
        }
    }
}