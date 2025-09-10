# Change Log

## Version 2.5.0

- Remove usage of unencrypted `StoreActivityLogs` endpoint
  
## Version 2.4.0

- Update tracking schema to 1.26.4
  - Deprecate `Sync` and `Performance` event

## Version 2.3.1

- Update server-api to 22.0.0

## Version 2.3.0

- Update Server Api to handle Properties field in ActivityLogs

## Version 2.2.0

- Update ActivityLogService and ActivityLogErrorReporter to support encrypted logs

## Version 2.1.0

- Add sessionStarted and sessionEnded

## Version 2.0.0

- Refactor mockito to mockk
- Refactor ActivityLogs authorization

## Version 1.12.1

- Update ItemId to a Kotlin data class

## Version 1.12.0

- Add Frozen Account state tracking

## Version 1.11.15

- Update server-api to 19.16.1

## Version 1.11.14

- Fix tests

## Version 1.11.13

- Update server-api to 19.13.0

## Version 1.11.12

- Update server-api to 19.12.0

## Version 1.11.11

- Update server-api to 19.11.7

## Version 1.11.10

- Update server-api to 19.11.2

## Version 1.11.9

- Update server-api to 19.10.5

## Version 1.11.8

- Update server-api to 19.10.4

## Version 1.11.7

- Update server-api to 19.10.3

## Version 1.11.6

- Update server-api to 19.10.2

## Version 1.11.5

- Update server-api to 19.10.0

## Version 1.11.4

- Update server-api to 19.9.3

## Version 1.11.3

- Update server-api to 19.9.0

## Version 1.11.1

- Update server-api to 19.8.9

## Version 1.10.3

- Update server-api to 19.8.8

## Version 1.10.2

- Update server-api to 19.8.7

## Version 1.10.1

- Update server-api to 19.8.5

## Version 1.10.0

- Add support to dependency injection with HiltWorker for `HermesWorker`

## Version 1.9.5

- Update server-api to 19.8.4

## Version 1.9.4

- Update cryptography to 2.6.0 and server-api to 19.8.2

## Version 1.9.3

- Fix HermesWorker initialization

## Version 1.9.2

- Remove component and application

## Version 1.9.1

- Add library signing information

## Version 1.9.0

- Switch to Gitlab maven registry

## Version 1.8.0
- New: Support for `ActivityLog`
  - `queueEvent(ActivityLog)` is now available in the API
  - `TrackingRepository` constructor now needs an `analyticsLogService` and an `activityLogService`
  - `activityLog()` helper function for creating easily an `ActivityLog`

## Version 1.7.4
- Fix tests
- Update tracking schema to 1.12.8

## Version 1.7.2
- Fix: Catch uncaught `CryptographyException` exception on empty file that was crashing 
  `HermesWorker` and prevented the flush of the logs stored on the device.

## Version 1.7.1
- Fix: mix missing VERSION variable
- Remove: tracking version resolution

## Version 1.7
- New: Update the Hermes versioning format to specify the version of
  the [tracking-schema](https://gitlab.dashlane.com/da/data/public/tracking-schemas)
  specs used.
- New: Add a manual job on Tag's Pipelines to deploy a new version of the release with the
  latest `Tracking-schema` specs.

## Version 1.6.10
- Fix: CI secrets

## Version 1.6.9
- Update: Crypo 2.4.1 which contains a binary-incompatible change

## Version 1.6.8
- Fix: Fix wrong format of Sha256Hash in a list

## Version 1.6.7
- New: Add new page view to identify from which section of the app a multi-url domain is associated to a credential (website view vs detailed view)
- New: Add a new value in field_list (multi_url_website) to flag a new website is associated to a credential
- New: Add new event for linked websites associated_domains_added_list and associated_domains_removed_list

## Version 1.6.6
- Update: item_id are filtered according to the Regex defined by Data.
- Update: generate code from spec 0.193.0

## Version 1.6.5
- New: Can set authenticator app platform

## Version 1.6.4
- Fix: uncaught deprecated [KeyStoreConnectException] exception on Android 8 & 8.1.

## VERSION 1.6.3
- Update: bump work library version to support android 12.

## VERSION 1.6.2
- Update: allow item id surrounded by whitespace characters.

## VERSION 1.6.1
- Update: Don't sanitize malformed item ids but use the fallback value.
- Fix: Fix wrong format for item_id property.

## VERSION 1.6
- New: Generate ItemId class to sanitize vault item ids for Data.
- New: Tag generated class with @Deprecated tag when flagged as deprecated in the Data spec 
- Update: Add Description to more generated class.

## Version 1.5.7
- Update: generate code from spec 0.158.0

## Version 1.5.6
- Update: introduce ConfigurationUtil to inject external configurations

## Version 1.5
- Update: generate code from spec 0.119.0
- Fix: breaking change, user installation and device+user ids are no more Java.UUID but String.
- Fix: Update unit tests to use new version of the Autofill logs.

## Version 1.4
- Fix: Fix broken CI for previously release

## Version 1.3
- Change: Add `Sha256Hash` type

## Version 1.2
- Fix: Date-time and locale formatting

## Version 1.1
- New: Set supported tracking schema version to 0.+

## Version 1.0.4
- Update: generate code from spec 0.80.0

## Version 1.0.3
- Update: generate code from spec 0.73.0
- Fix: Browse section no longer sent for anonymous logs

## Version 1.0.2
- Update: generate code from spec 0.68.0

## Version 1.0.1
- Update: generate code from spec 0.59.0

## Version 1.0.0
- New: Add support to `anonymous` logs

## Version 0.5.4
- Fix: Error when serializing lists

## Version 0.5.3
- New: `AnalyticsIdRepository` interface and its implementation
- Changed: `LogRepository` inherit `AnalyticsIdRepository`
- Changed: `HermesComponent` exposes `LogRepository` instead of its implementation `TrackingRepository`
- Update: generate code from spec 0.39.0

## Version 0.5.2
- Fix: Catch `GeneralSecurityException` and `IOException` thrown by the `security-crypto` dependency 

## Version 0.5.1
- Fix: add `Browse` section to logs even if no page view info are available.
- Update: generate code from spec 0.30.0
- Changed: `refresh_schemas.sh` now clean previous artifact before pulling and extracting the target artifact
- Changed: `:json-schema-tracking-generator:run` clean previously generated code before running the generator
- Add: Debug mode to `SecureFileLogStorage` to print logs to terminal as they are stored
- Add: Debug mode to `TrackingRepository` to flush log events as they go instead of storing them in the queue. 

## Version 0.5.0
- New: priority flag supported by the code generator, `TrackingLog` now has a `priority` property
- Changed: `TrackingRepository` constructor now takes a `LogFlush` object and will call its `invoke()` 
method whenever a priority `Page` or `TrackingLog` is enqueued to flush the whole log queue.    

## Version 0.4.2
- New: `SecureFileLogStorage` to provide a secure way for storing logs on disk.
- Changed: fetch tracking specs from releases artifacts
- Changed: generated logs now use the schema-version based on the release used to generate code.

## Version 0.4.1
- Fix: PageView logs now contain `category` and `properties` sections.
- Changed: renamed `LogRepository#enqueueBrowse` to `LogRepository#enqueuePageView` for consistency with log type naming. 

## Version 0.4
- Update: LogRepository#queueBrowse method signature
- Update: Regenerate logs from latest specs

## Version 0.3.1
- Update: only Browse log events can create a new tracking session and keep the session alive. 
 
## Version 0.3.0
- New: `LogFlush` abstraction for sending logs
- New: `LogFlushLifecycleObserver` that triggers log flush when paused
- New: Android worker to schedule log flush
- New: Mechanism to identify test logs using `AnalyticsLogService.TestLogChecker`
- Update: Remove `date_time` property on logs in favor of `date`

## Version 0.2.0
- new: Working implementation of `LogService`: `AnalyticsLogService` for sending logs to Styx.
- `TrackingRepositoryImpl` now handle exception raised by the `event` endpoints.

## Version 0.1.0

- New: Code generator to generate Events and Definitions from the tracking-schemas specs: https://gitlab.dashlane.com/da/data/public/tracking-schemas
- New: Hermes SDK to handle building logs, event queuing/batching and flush events to the Styx API.
