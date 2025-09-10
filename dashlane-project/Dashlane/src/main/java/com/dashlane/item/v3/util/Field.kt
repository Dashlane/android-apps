package com.dashlane.item.v3.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class SensitiveField {
    PASSWORD,
    CREDIT_CARD_NUMBER,
    CREDIT_CARD_SECURITY_CODE,
    CREDIT_CARD_NOTE,
    WIFI_PASSPHRASE,
}

@Composable
fun rememberIsSensitiveFieldRevealed(
    revealedFields: Set<SensitiveField>,
    field: SensitiveField,
): Boolean = remember(revealedFields, field) {
    revealedFields.contains(field).not()
}


enum class Field {
    CREDIT_CARD_OWNER_NAME,
    CREDIT_CARD_SECURITY_CODE,
    CREDIT_CARD_EXPIRATION_DATE,
    CREDIT_CARD_NOTE,
    CREDIT_CARD_ITEM_NAME,
    WIFI_PASSPHRASE,
    WIFI_NOTE,
}

@Composable
fun rememberIsFieldVisible(hiddenFields: Set<Field>, field: Field): Boolean =
    remember(hiddenFields, field) { hiddenFields.contains(field).not() }