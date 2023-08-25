package com.dashlane.navigation

object NavigationHelper {

    object Destination {
        const val SCHEME = "dashlane"

        object MainPath {
            
            const val PASSWORDS = "passwords"
            const val NOTES = "notes"
            const val ADDRESSES = "addresses"
            const val BANK_ACCOUNTS = "bank-accounts"
            const val COMPANIES = "companies"
            const val DRIVER_LICENSES = "driver-licenses"
            const val EMAILS = "emails"
            const val FISCAL = "fiscal"
            const val ID_CARDS = "id-cards"
            const val IDENTITIES = "identities"
            const val PASSPORTS = "passports"
            const val WEBSITES = "websites"
            const val CREDIT_CARDS = "credit-cards"
            const val PAYPAL_ACCOUNTS = "paypal-accounts"
            const val PHONES = "phones"
            const val SOCIAL_SECURITY_NUMBERS = "social-security-numbers"

            
            const val GET_PREMIUM = "getpremium"
            const val SEARCH = "search"
            const val ITEMS = "items"

            
            const val PASSWORD_GENERATOR = "password-generator"
            const val PASSWORD_HEALTH = "password-health"
            const val SHARING_CENTER = "sharing"
            const val VPN = "vpn"
            const val LOGIN = "login"
            const val IN_APP_LOGIN = "in-app-login"
            const val CSV_IMPORT = "csv-import"
            const val DARK_WEB_MONITORING = "dark-web-monitoring"
            const val DARK_WEB_MONITORING_PREMIUM_PROMPT = "dark-web-monitoring-premium-prompt"
            const val AUTHENTICATOR_TOOL = "authenticator"
            const val SECRET_TRANSFER = "mplesslogin"

            
            const val PAYMENTS = "payments"
            const val PERSONAL_INFO = "personal-info"
            const val ID_DOCUMENT = "id-documents"

            
            const val SETTINGS = "settings"
        }

        object SecondaryPath {
            object GetPremium {
                const val ESSENTIALS_OFFER = "essentialsoffer"
                const val PREMIUM_OFFER = "premiumoffer"
                const val FAMILY_OFFER = "premiumfamilyoffer"
            }

            object Items {
                const val NEW = "new"
                const val SHARE = "share"
                const val SHARE_INFO = "shareInfo"
            }

            object PasswordHealth {
                const val COMPROMISED = "compromised"
                const val REUSED = "reused"
                const val WEAK = "weak"
            }

            object SettingsPath {
                const val GENERAL = "general"
                const val SECURITY = "security"
            }
        }

        object PathQueryParameters {
            const val FROM = "from"
            const val IS_PACKAGE_NAME = "isPackageName"

            object CsvImport {
                const val URI = "uri"
            }
        }
    }
}
