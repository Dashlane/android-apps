package com.dashlane.search

data class MatchedSearchResult(val item: Any, val match: Match)

data class Match(val position: MatchPosition, val field: SearchField<*>)

enum class FieldType {
    PRIMARY,
    SECONDARY,
}

enum class MatchPosition {
    START,
    ANYWHERE;
}

enum class ItemType {
    CREDENTIAL,
    PASSKEY,
    BANK_STATEMENT,
    CREDIT_CARD,
    PAYPAL,
    SECURE_NOTE,
    DRIVER_LICENCE,
    FISCAL_STATEMENT,
    ID_CARD,
    PASSPORT,
    SOCIAL_SECURITY_STATEMENT,
    ADDRESS,
    COMPANY,
    EMAIL,
    IDENTITY,
    PERSONAL_WEBSITE,
    PHONE_NUMBER,
    SETTING,
    UNSUPPORTED;
}
