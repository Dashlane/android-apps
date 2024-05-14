package com.dashlane.secrettransfer.view

import com.dashlane.server.api.endpoints.secrettransfer.GetKeyExchangeTransferInfoService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed class SecretTransferState {

    data object Initial : SecretTransferState()
    data object Loading : SecretTransferState()
    data class GoToIntro(val isPasswordless: Boolean) : SecretTransferState()
    data class ShowTransfer(val transfer: SecretTransfer) : SecretTransferState()
}

data class SecretTransfer(
    val id: String,
    val deviceName: String,
    val city: String,
    val countryCode: String,
    val hashedPublicKey: String,
    val formattedDate: String,
)

fun GetKeyExchangeTransferInfoService.Data.Transfer.toSecretTransfer() = SecretTransfer(
    id = this.transferId,
    deviceName = this.receiver.deviceName,
    city = this.receiver.city,
    countryCode = this.receiver.countryCode,
    hashedPublicKey = this.receiver.hashedPublicKey,
    formattedDate = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .format(Instant.ofEpochSecond(this.receiver.requestedAtDate.epochSecond).atZone(ZoneId.systemDefault()))
)
