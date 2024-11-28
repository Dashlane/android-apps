# Android apps

This repository contains the source code for our Android password manager application. It is publicly available for everyone to audit our code and learn more about how our Android applications work.

We are working with a modularized app, and we plan to publicly release the source code of more modules.

## History

The Android project was started back in 2010 and was written in Java with some usage of native libraries (e.g. cryptography was using a C++ library in common with our desktop apps). We are doing continuous refactoring and adopting new tech, as a consequence, we also have legacy code as our migration plans often focus on new features and code that changes frequently. For example, most of our codebase is now in Kotlin, but we still have a few java classes.

## High-level architecture

### Codebase organization

The app used to be monolithic in a single repository and was progressively modularized. Some modules are directly put in our repository while others are built separately and published on a private maven repository to be used by our apps. Overall modularization helped us with our build processes, while also allowing us to reuse modules.

### UI architecture pattern

Our current standard architecture pattern used for the views is MVVM. It helps us isolate the business logic in clear layers and components. However, we used MVP for years and not everything is migrated yet to MVVM.


### Kotlin, coroutines, and flows

We have adopted kotlin and most but not all of the codebase is converted, so we still have some legacy java classes.

We are making extensive use of Coroutines and Flows in the app. We used a lot of the experimental coroutine APIs (e.g. actors) and now migrate them to Flows, especially where we already migrated to MVVM.

### Dependency injection

After a long time of using Dagger, we moved to use Hilt at the end of 2021 and used this migration as an opportunity to reduce the number of things that we were getting through our SingletonComponent.


### Navigation

We have a mix of fragments and activities and navigation is handled through the Jetpack Navigation library. See `NavigatorImpl.kt` or `drawer_navigation.xml` as a starter to explore it.


## Cryptography

Dashlane is heavy using Cryptography to protect the data of the users. We use the [OpenSSL](https://www.openssl.org/) library and [Argon2](https://github.com/P-H-C/phc-winner-argon2) as cryptography primitive functions to build algorithms for Dashlane. The algorithms serve for Symmetric Cryptography, Asymmetric Cryptography, and Key Derivation.

If you want to learn more about cryptography at Dashlane, have a look at our [Security Whitepaper](https://www.dashlane.com/download/whitepaper-en.pdf).


## How to contribute

### Security issue

If you find a vulnerability or a security issue, please report it on our [Hacker One Bug Bounty program](https://hackerone.com/dashlane).

### Codebase improvement

If there is an improvement for the codebase you would like to share with us, we would be happy to hear your thoughts! Feel free to open an issue on this repository or reach us at dev-relationship@dashlane.com.

## Get our apps

|  Dashlane Apps |  Download link | 
|---|---|
| Dashlane Password Manager   |  <a href="https://play.google.com/store/apps/details?id=com.dashlane"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height=100px /></a>  |