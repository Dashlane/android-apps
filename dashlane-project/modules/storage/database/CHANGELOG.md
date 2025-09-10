# Change Log

## Version 6.5.3

- Change:
  - Bump vault-xml dependency
  - Update test data with Cloud passkey item

## Version 6.5.2

- Change:
  - Remove PaymentPaypal
  - Catch Json Exception caused by XmlTypeException to skip unsupported data type

## Version 6.5.1

- Change:
  - Add keyAlgorithm to `SummaryObject.Passkey` to be able to distinguish Cloud Passkeys from regular Passkeys

## Version 6.5.0

- Change:
  - Fix API to rename database

## Version 6.4.0

- Change:
  - Add API to rename database

## Version 6.3.0

- Change:
  - Add wifi type support

## Version 6.2.4

- Change:
  - Add support to China Union Pay, Maestro, uatp, dankort and interpayment types

## Version 6.2.3

- Change:
  - Add country and isNoteEmpty in `SummaryObject.PaymentCreditCard`
  - Update dependency com.google.devtools.ksp to v2.0.21-1.0.28

## Version 6.2.2

- Change:
  - Update cryptography to 2.7.1
  - Update Compile SDK to 35

## Version 6.2.1

- Change:
  - Add lastUsed in `SummaryObject.Authentifiant`
  
## Version 6.2.0

- Change:
  - Add Secret support

## Version 6.1.2

- Change:
  - Remove deprecated functions SyncObject.toSummary
  - Fix cast crash when data type miss match

## Version 6.1.1

- Change:
  - Update vault XML to 4.13.0
  - Cleanup last ref to anonId

## Version 6.1.0

- Change:
  - Update vault XML to 4.12.0
  - Remove `anonId` from all `SummaryObject.CommonSummary`

## Version 6.0.1

- Change:
  - DatabaseProvider `isAccessible` and `open` functions are now called on the ioDispatcher

## Version 6.0.0

- Breaking changes:
  - Most of the public APIs have been refactored to either be suspending functions or require
    an IO dispatcher to access files
  - Database creation now requires a Deferred<File> so it can be initialized in an async manner
  - DatabaseProvider now requires an IO dispatcher to access files

## Version 5.4.0

- Change:
  - Deprecate `SyncObject.toSummary()` and `SyncObject.toSummaryOrNull()`

## Version 5.3.1

- Change:
  - Update vault XML to 4.11.1

## Version 5.2.0

- Change:
  - Remove `privateKey` from `SummaryObject.Passkey`

## Version 5.1.0

- Change:
  - Simply DatabaseSummary data structure
  
## Version 5.0.0

- Change:
  - Add support for Sharing Collection

## Version 4.7.0

- Change:
  - Update vault XML to 4.11.0
  - Add `ItemName` and `Note` to `SummaryObject.Passkey`

## Version 4.6.0

- Change:
  - Upgrade cryptography 2.6.0

## Version 4.5.0

- Change:
  - Add SupportedTypeTest
  
## Version 4.4.3

- Change:
  - Add passkey summary

## Version 4.4.1

- Change:
  - Add library signing

## Version 4.4.0

- Change:
  - Generated ids are now uppercase

## Version 4.3.1

- Fix:
  - Use else branch in the when for toSummaryOrNull

## Version 4.3.0

- Change:
  - Update Vault XML to 4.9.0
  - Update Cryptography to 2.5.0

## Version 4.2.1

- Change:
  - Add func to print db information

## Version 4.2.0

- Change:
  - Update Vault XML to 4.6.0
  - Add support for Collection

## Version 4.1.1.1

- Fix:
  - Use else branch in the when for toSummaryOrNull

## Version 4.1.1

- Change:
  - Remove duplicated SyncObjectTypesUtils

## Version 4.1.0

- Change:
  - Reject unsupported types writing

## Version 4.0.9

- Change:
  - Update Vault XML to 4.4.0

## Version 4.0.8

- Fix:
  - Skip unsupported SyncDataType when building Summary
  
## Version 4.0.7

- Change:
  - Update Vault XML to 4.3.1 and fix enum serialization

## Version 4.0.6

  - Change:
    - Update Vault XML to 4.3.0

## Version 4.0.5

    - Change:
        - Bump to Vault XML 4.2.0
        - Add IsFavorite field

## Version 4.0.4

    - Change:
        - More reliable snapshots test

## Version 4.0.3

    - Change:
        - Add sha512 inside the `AssociatedAndroidApps` object

## Version 4.0.2

    - Fix:
        - Increment summary version to fix linked services parsing issue

## Version 4.0.1

    - Change:
        - Add android apps to linked websites

## Version 4.0.0

    - Change:
        - Use `Year` and `Month` types introduced in vault XML domain 4.0.0.
    - Fix:
        - Use `fsync` to make sure changes are correctly written to disk in transaction.

## Version 3.0.2

    - Change:
        - Fix uncaught exception in SearchRepositoryImpl

## Version 3.0.1
    - Add:
        - Add field bankAccountCountry to SummaryObject.BankStatement

## Version 3.0.0

    - Change:
        - Use `Country` type introduced in Vault XML 3.0
    - Removed
        - No longer publish empty `database-android` artifact

## Version 2.5.2

    - Fix
        - Rebuild summary for fixing appMetaData field

## Version 2.5.1
    - Fix
        - Fix NPE in VaultObjectRepositoryImpl get()

## Version 2.5.0

 - Update
    - Update `AppMetaData` in `Authentifiant` to be an object rather that a raw JSON string

## Version 2.4.0

- Update
    - Add hasOtpUrl in SummaryObject.Authentifiant

## Version 2.3.0

- Update
    - Add `linkedServices` field for authentifiant and update vault-xml library

## Version 2.2.0

- Fix
    - Can't open database if rebuild summary with pending transaction

## Version 2.1.2

- Fix
    - MemorySummaryRepository init is suspend function
    - Catch file not found exception when reading
    - Cryptography 2.2.1 to fix leaking of open files

## Version 2.1.1

- Fix
    - Merge 2.1.0 version with 2.0.1. Same fix as 2.0.1.

## Version 2.1.0

- Update
    - Add `fullname` and `linkedIdentity` field to [SummaryObject.FiscalStatement]

## Version 2.0.1

- Fix
    - transaction directory should be cleaned

## Version 2.0.0

- Update
    - Write transaction for Sharing and Search

## Version 1.2.1

- fix
    - Update [DatabaseIndex.summaryVersion] that was forgotten in 1.2.0

## Version 1.2.0

- Update
    - Add missing `note` field to [SummaryObject.Authentifiant]

## Version 1.1.1

- Fix
  - Use databases as directory name

## Version 1.1.0

- Fix
  - Database root per user should be under database directory
- Feature
  - When migration is needed, rebuild the summary files when open database
- Others
  - Force key type as Raw32
  - Force cryptogaphy marker as CryptographyMarker.Flexible.Defaults.noDerivation

## Version 1.0.0

Initial Version with feature completed
