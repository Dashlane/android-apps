# Hermes Logs SDK

Welcome to the Hermes SDK, AKA the new client event tracking system aimed to replace  
https://gitlab.dashlane.com/android/android-activity-logs.

## Overview

This project is the Android version of the client SDK. For other platforms please check

* iOS/Safari: https://gitlab.dashlane.com/apple/common/dashlanereportkit/-/tree/feature/hermes
* Leeloo/web extension: https://gitlab.dashlane.com/webproduct/hermes

This repository is separated into modules with different purposes:

* `json-schema-tracking-generator` - Parser and code generator which translates the Data Json schema
  into Kotlin code living in the `hermes` Java library.
* `hermes` - Produces a Java library which contains all Definition and Event files generated above.
  It also contains the logic for log tracking events to the Data web API: Styx
* `hermes-android` - Which depends on and exposes `hermes`, is an Android libs that defines android
  components such as a worker, a lifecycle observer, a Dagger component for DI and an filesystem
  implementation of `LogStorage`.

## Generating tracking specs

If the desired tracking spec version is higher than
the [ones that is currently supported](https://gitlab.dashlane.com/android/hermes/-/blob/master/build.gradle.kts#L46)
, Go in the root gradle build and bump up `downloadTrackingSchema` version

```shell
./gradlew downloadTrackingSchema
```

Then to generate the code from the specs simply run the following gradle command:

```shell
./gradlew json-schema-tracking-generator:run
```

the generator will then process the schema and generate the according code that will serve as
building blocks to create logs. All generated code can be found in the `hermes` module at
`hermes/src/main/kotlin/com/dashlane/hermes/generated/`.

## FAQ

### How to push a version containing the [latest supported schema version](https://gitlab.dashlane.com/android/hermes/-/blob/master/build.gradle.kts#L46) on maven?
