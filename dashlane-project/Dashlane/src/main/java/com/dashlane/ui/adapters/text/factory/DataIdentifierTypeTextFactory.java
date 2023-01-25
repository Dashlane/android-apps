package com.dashlane.ui.adapters.text.factory;

import android.annotation.SuppressLint;

import com.dashlane.R;
import com.dashlane.vault.model.DataIdentifierId;



public class DataIdentifierTypeTextFactory {

    @SuppressLint("SwitchIntDef")
    public static int getStringResId(@DataIdentifierId.Def int desktopId) {
        switch (desktopId) {
            case DataIdentifierId.ADDRESS:
                return R.string.datatype_address;
            case DataIdentifierId.AUTH_CATEGORY:
                return R.string.datatype_auth_category;
            case DataIdentifierId.AUTHENTIFIANT:
                return R.string.datatype_authentifiant;
            case DataIdentifierId.COMPANY:
                return R.string.datatype_company;
            case DataIdentifierId.DRIVER_LICENCE:
                return R.string.datatype_driver_licence;
            case DataIdentifierId.EMAIL:
                return R.string.datatype_email;
            case DataIdentifierId.FISCAL_STATEMENT:
                return R.string.datatype_fiscal_statement;
            case DataIdentifierId.GENERATED_PASSWORD:
                return R.string.datatype_address;
            case DataIdentifierId.ID_CARD:
                return R.string.datatype_id_card;
            case DataIdentifierId.IDENTITY:
                return R.string.datatype_identity;
            case DataIdentifierId.MERCHANT:
                return R.string.datatype_merchant;
            case DataIdentifierId.PASSPORT:
                return R.string.datatype_passport;
            case DataIdentifierId.PAYMENT_PAYPAL:
                return R.string.datatype_paymentpaypal;
            case DataIdentifierId.PAYMENT_CREDIT_CARD:
                return R.string.datatype_paymentcreditcard;
            case DataIdentifierId.PERSONAL_DATA_DEFAULT:
                return R.string.datatype_personaldata_default;
            case DataIdentifierId.PERSONAL_WEBSITE:
                return R.string.datatype_personal_website;
            case DataIdentifierId.PHONE:
                return R.string.datatype_phone;
            case DataIdentifierId.PURCHASE_ARTICLE:
                return R.string.datatype_purchase_article;
            case DataIdentifierId.PURCHASE_BASKET:
                return R.string.datatype_purchase_basket;
            case DataIdentifierId.PURCHASE_CATEGORY:
                return R.string.datatype_purchase_category;
            case DataIdentifierId.PURCHASE_CONFIRMATION:
                return R.string.datatype_purchase_confirmation;
            case DataIdentifierId.SOCIAL_SECURITY_STATEMENT:
                return R.string.datatype_social_security_statement;
            case DataIdentifierId.SECURE_NOTE:
                return R.string.datatype_secure_note;
            case DataIdentifierId.SECURE_NOTE_CATEGORY:
                return R.string.datatype_secure_note_category;
            case DataIdentifierId.BANK_STATEMENT:
                return R.string.datatype_bank_statement;
            default:
                return 0;
        }
    }
}
