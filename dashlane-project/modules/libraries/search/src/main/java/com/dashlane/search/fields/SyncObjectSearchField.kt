package com.dashlane.search.fields

import com.dashlane.search.FieldType
import com.dashlane.search.ItemType
import com.dashlane.search.SearchField
import com.dashlane.xml.domain.SyncObject

enum class CredentialField(
    override val order: Int,
    override val itemType: ItemType = ItemType.CREDENTIAL,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Authentifiant> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    EMAIL(order = 2),
    LOGIN(order = 3),
    BUNDLE_WEBSITES(order = 4),
    LINKED_DOMAIN(order = 5),
    NOTE(order = 6),
    SECONDARY_LOGIN(order = 7),
    URL(order = 8),
    USER_SELECTED_URL(order = 9);
}

enum class PasskeyField(
    override val order: Int,
    override val itemType: ItemType = ItemType.PASSKEY,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Passkey> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    USERNAME(order = 2),
    WEBSITE(order = 3),
    NOTE(order = 4);
}


enum class BankStatementField(
    override val order: Int,
    override val itemType: ItemType = ItemType.BANK_STATEMENT,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.BankStatement> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    BANK(order = 2),
    OWNER(order = 3);
}

enum class CreditCardField(
    override val order: Int,
    override val itemType: ItemType = ItemType.CREDIT_CARD,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.PaymentCreditCard> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    BANK(order = 2),
    OWNER(order = 3);
}


enum class SecureNoteField(
    override val order: Int,
    override val itemType: ItemType = ItemType.SECURE_NOTE,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.SecureNote> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    CONTENT(order = 2);
}


enum class SecretField(
    override val order: Int,
    override val itemType: ItemType = ItemType.SECRET,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.SecureNote> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
}


enum class DriverLicenceField(
    override val order: Int,
    override val itemType: ItemType = ItemType.DRIVER_LICENCE,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.DriverLicence> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    FULL_NAME(order = 2);
}

enum class FiscalStatementField(
    override val order: Int,
    override val itemType: ItemType = ItemType.FISCAL_STATEMENT,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.FiscalStatement> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    FULL_NAME(order = 2);
}

enum class IdCardField(
    override val order: Int,
    override val itemType: ItemType = ItemType.ID_CARD,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.IdCard> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    FULL_NAME(order = 2);
}

enum class PassportField(
    override val order: Int,
    override val itemType: ItemType = ItemType.PASSPORT,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Passport> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    FULL_NAME(order = 2);
}

enum class SocialSecurityStatementField(
    override val order: Int,
    override val itemType: ItemType = ItemType.SOCIAL_SECURITY_STATEMENT,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.SocialSecurityStatement> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    SOCIAL_SECURITY_FULL_NAME(order = 2);
}


enum class AddressField(
    override val order: Int,
    override val itemType: ItemType = ItemType.ADDRESS,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Address> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    FULL(order = 2),
    BUILDING(order = 3),
    CITY(order = 4),
    DOOR(order = 5),
    FLOOR(order = 6),
    STREET_NAME(order = 7),
    ZIP(order = 7);
}

enum class CompanyField(
    override val order: Int,
    override val itemType: ItemType = ItemType.COMPANY,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Company> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    JOB_TITLE(order = 2);
}

enum class EmailField(
    override val order: Int,
    override val itemType: ItemType = ItemType.EMAIL,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Email> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    EMAIL(order = 2);
}

enum class IdentityField(
    override val order: Int,
    override val itemType: ItemType = ItemType.IDENTITY,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Identity> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    MIDDLE_NAME(order = 2),
    LAST_NAME(order = 3),
    PSEUDO(order = 4);
}

enum class PersonalWebsiteField(
    override val order: Int,
    override val itemType: ItemType = ItemType.PERSONAL_WEBSITE,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.PersonalWebsite> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1),
    WEBSITE(order = 2);
}

enum class PhoneField(
    override val order: Int,
    override val itemType: ItemType = ItemType.PHONE_NUMBER,
    override val fieldType: FieldType = FieldType.SECONDARY
) : SearchField<SyncObject.Phone> {
    TITLE(order = 0, fieldType = FieldType.PRIMARY),
    ITEM_TYPE_NAME(order = 1);
}
