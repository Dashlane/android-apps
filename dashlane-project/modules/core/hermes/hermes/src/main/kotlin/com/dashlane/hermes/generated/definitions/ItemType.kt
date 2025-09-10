package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ItemType(override val code: String) : TrackingLog.Enum {
    ADDRESS(
        code = "address",
    ),
    BANK_STATEMENT(
        code = "bank_statement",
    ),
    COMPANY(
        code = "company",
    ),
    CREDENTIAL(
        code = "credential",
    ),
    CREDIT_CARD(
        code = "credit_card",
    ),
    DRIVER_LICENSE(
        code = "driver_license",
    ),
    EMAIL(
        code = "email",
    ),
    FISCAL_STATEMENT(
        code = "fiscal_statement",
    ),
    GENERATED_PASSWORD(
        code = "generated_password",
    ),
    ID_CARD(
        code = "id_card",
    ),
    IDENTITY(
        code = "identity",
    ),
    PASSKEY(
        code = "passkey",
    ),
    PASSPORT(
        code = "passport",
    ),
    PAYPAL(
        code = "paypal",
    ),
    PHONE(
        code = "phone",
    ),
    SECRET(
        code = "secret",
    ),
    SECURE_NOTE(
        code = "secure_note",
    ),
    SECURITY_BREACH(
        code = "security_breach",
    ),
    SOCIAL_SECURITY(
        code = "social_security",
    ),
    WEBSITE(
        code = "website",
    ),
    WIFI(
        code = "wifi",
    ),
}