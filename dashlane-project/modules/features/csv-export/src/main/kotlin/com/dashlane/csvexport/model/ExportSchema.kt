package com.dashlane.csvexport.model

enum class AuthentifiantExport(val value: String) {
    USER_NAME("user_name"),
    USER_NAME_2("user_name_2"),
    USER_NAME_3("user_name_3"),
    TITLE("title"),
    PASSWORD("password"),
    NOTE("note"),
    URL("url"),
    CATEGORY("category"),
    OTP("otp"),
}

enum class SecureNoteExport(val value: String) {
    TITLE("title"),
    NOTE("note"),
    CATEGORY("category"),
}

enum class PaymentExport(val value: String) {
    ACCOUNT_NAME("accountName"),
    ACCOUNT_HOLDER("accountHolder"),
    CC_NUMBER("ccNumber"),
    CODE("code"),
    EXPIRATION_MONTH("expirationMonth"),
    EXPIRATION_YEAR("expirationYear"),
    ROUTING_NUMBER("routingNumber"),
    ACCOUNT_NUMBER("accountNumber"),
    COUNTRY("country"),
    ISSUING_BANK("issuingBank"),
    NOTE("note"),
    ITEM_NAME("name"),
}

enum class IdExport(val value: String) {
    NUMBER("number"),
    ISSUE_DATE("issueDate"),
    EXPIRATION_DATE("expirationDate"),
    PLACE_OF_ISSUE("placeOfIssue"),
    STATE("state"),
    ITEM_NAME("name"),
}

internal const val TYPE = "type"

enum class PaymentType(val value: String) {
    PAYMENT_CARD("payment_card"),
    BANK("bank"),
}

enum class IdType(val value: String) {
    CARD("card"),
    PASSPORT("passport"),
    LICENSE("license"),
    SOCIAL_SECURITY("social_security"),
    TAX_NUMBER("tax_number"),
}

enum class PersonalInfoExport(val value: String) {
    
    TITLE("title"),
    FIRST_NAME("firstName"),
    MIDDLE_NAME("middleName"),
    LASTNAME("lastName"),
    LOGIN("login"),
    DATE_OF_BIRTH("dateOfBirth"),
    PLACE_OF_BIRTH("placeOfBirth"),

    
    EMAIL("email"),
    EMAIL_TYPE("emailType"),

    
    ITEM_NAME("itemName"),

    
    PHONE_NUMBER("phoneNumber"),

    
    ADDRESS("address"),
    COUNTRY("country"),
    STATE("state"),
    CITY("city"),
    ZIP("zip"),
    ADDRESS_RECIPIENT("addressRecipient"),
    ADDRESS_BUILDING("addressBuilding"),
    ADDRESS_APARTMENT("addressApartment"),
    ADDRESS_FLOOR("addressFloor"),
    ADDRESS_DOOR_CODE("addressDoorCode"),

    
    JOB_TITLE("jobTitle"),

    
    URL("url"),
}

enum class ContactType(val value: String) {
    CONTACT_NAME("name"),
    EMAIL("email"),
    NUMBER("number"),
    ADDRESS("address"),
    COMPANY("company"),
    WEBSITE("website"),
}