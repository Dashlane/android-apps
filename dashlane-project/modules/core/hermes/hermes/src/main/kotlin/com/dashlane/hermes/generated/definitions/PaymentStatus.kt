package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PaymentStatus(override val code: String) : TrackingLog.Enum {
    ASSIGNED_PLAN_SAME_TIER_AS_CURRENT_PLAN(
        code = "assigned_plan_same_tier_as_current_plan",
    ),
    B2B_PLAN_NOT_FOUND(
        code = "b2b_plan_not_found",
    ),
    CANNOT_INVOICE_FREE_PLAN(
        code = "cannot_invoice_free_plan",
    ),
    CHANGE_TIER_AMOUNT_TOO_EXPENSIVE(
        code = "change_tier_amount_too_expensive",
    ),
    CURRENT_PLAN_START_DATE_IN_FUTURE(
        code = "current_plan_start_date_in_future",
    ),
    ERROR_CALCULATING_TIER_CHANGE(
        code = "error_calculating_tier_change",
    ),
    FORBIDDEN_EMAIL_DOMAIN(
        code = "forbidden_email_domain",
    ),
    INSUFFICIENT_AMOUNT(
        code = "insufficient_amount",
    ),
    INVALID_PLAN(
        code = "invalid_plan",
    ),
    NO_KEY_FOR_USER(
        code = "no_key_for_user",
    ),
    NOT_BILLING_ADMIN(
        code = "not_billing_admin",
    ),
    NOT_ELIGIBLE_TO_CHANGE_TIER_MID_CYCLE(
        code = "not_eligible_to_change_tier_mid_cycle",
    ),
    NOT_IN_A_TEAM_PLAN(
        code = "not_in_a_team_plan",
    ),
    NOT_IN_FREE_TRIAL(
        code = "not_in_free_trial",
    ),
    NOT_TEAM_CAPTAIN(
        code = "not_team_captain",
    ),
    PAYMENT_ERROR_DURING_UPGRADE(
        code = "payment_error_during_upgrade",
    ),
    PAYMENT_MEAN_IS_NOT_CREDIT_CARD(
        code = "payment_mean_is_not_credit_card",
    ),
    PAYMENT_MEAN_IS_NOT_INVOICE(
        code = "payment_mean_is_not_invoice",
    ),
    SALES_TAX_MISMATCH(
        code = "sales_tax_mismatch",
    ),
    SEATS_TO_PURCHASE_LOWER_ACTUAL_USAGE(
        code = "seats_to_purchase_lower_actual_usage",
    ),
    SELECTED_PLAN_IS_NOT_VALID(
        code = "selected_plan_is_not_valid",
    ),
    STARTER_PLAN_NOT_10_SEATS(
        code = "starter_plan_not_10_seats",
    ),
    SUCCESS(
        code = "success",
    ),
    TEAM_HAS_SSO_ENABLED(
        code = "team_has_sso_enabled",
    ),
    UNSUPPORTED_PAYMENT_MEAN(
        code = "unsupported_payment_mean",
    ),
    USER_SUBSCRIBING_NOT_FOUND(
        code = "user_subscribing_not_found",
    ),
    VAT_NUMBER_NOT_UPSERTED(
        code = "vat_number_not_upserted",
    ),
    WRONG_AMOUNT_TO_PAY(
        code = "wrong_amount_to_pay",
    ),
}