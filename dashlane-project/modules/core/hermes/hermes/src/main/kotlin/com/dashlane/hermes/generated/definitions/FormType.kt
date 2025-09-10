package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class FormType(override val code: String) : TrackingLog.Enum {
    BILLING(
        code = "billing",
    ),
    CHANGE_PASSWORD(
        code = "change_password",
    ),
    CONTACT(
        code = "contact",
    ),
    FORGOT_PASSWORD(
        code = "forgot_password",
    ),
    IDENTITY(
        code = "identity",
    ),
    LOGIN(
        code = "login",
    ),
    NEWSLETTER(
        code = "newsletter",
    ),
    OTHER(
        code = "other",
    ),
    PAYMENT(
        code = "payment",
    ),
    REGISTER(
        code = "register",
    ),
    SEARCH(
        code = "search",
    ),
    SHIPPING(
        code = "shipping",
    ),
    SHOPPING_BASKET(
        code = "shopping_basket",
    ),
}