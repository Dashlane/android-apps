# Module cryptography

Symmetric cryptography engine. 

```kotlin
val cryptography = Cryptography(
    CipherFactory(),
    MacFactory(),
    KeyDerivationEngine(),
    SaltGenerator(),
    IvGenerator(),
    PaddingGenerator()
)

fun encrypt(data: ByteArray, password: String, salt: ByteArray): EncryptedByteArray =
    CryptographyKey.ofPassword(password)
        .use { key -> cryptography.createFlexibleArgon2dEncryptionEngine(key, CryptographyFixedSalt(salt)) }
        .use { encryptionEngine -> encryptionEngine.encryptByteArrayToByteArray(data, compressed = false) }

fun decrypt(encryptedData: EncryptedByteArray, password: String): ByteArray =
    CryptographyKey.ofPassword(password)
        .use { key -> cryptography.createDecryptionEngine(key) }
        .use { decryptionEngine -> decryptionEngine.decryptByteArrayToByteArray(encryptedData, compressed = false) }
```

## Marker

The cryptography marker indicates a type of cryptography to use. This
includes information on the algorithms for ciphering data as well as the
parameters for these algorithms that are shared across transactions. The
preferred value for encryption is stored in the user settings, based on
preferences defined by the user.

The `CryptographyMarker` class represents a marker, which can be parsed from 
a settings value using the `String.toCryptographyMarker()` method. The
`EncryptedByteArray.getCryptographyMarker` and
`EncryptedBase64String.getCryptographyMarker` methods read the cryptography
marker from the encrypted data header.

The header consists in the marker along with the additional parameters
which are stored in the data before the actual ciphered contents, and
which are passed to the cryptography algorithms. This is used internally
by the engine to keep track of the values such as initialization vector
and salt randomly generated for each transaction. The header combined
with the appropriate key allows the data to be decrypted.

## Fixed salt

The fixed salt enables the cache of key derivation results. It must be
provided during encryption for some markers, and is stored in the user
settings.

## Operations

To perform cryptographic operations on data, create a
[Cryptography] instance. This facade class acts as a factory for the engines 
used to perform encryption and decryption operations.

In the context of the application code, it can be easier to inject a
factory which hides the underlying task of providing the cryptography
key and determining the marker for encryption. For that purpose, use a
`CryptographyEngineFactory` instead, which is injected within its 
constructor, so that its factory methods don't require any parameters.

All engines should be closed after use, to clean up underlying resources
such as the cryptography key and its derived values.

Encrypted data is represented by dedicated types, to provide more
semantics and avoid confusion with plaintext data. Different types are
available for different encrypted formats, all represented using inline
classes.

| Format                 | AES Encrypted Data       |
|:-----------------------|:-------------------------|
| Byte Array             | `EncryptedByteArray`     |
| Base 64 Encoded String | `EncryptedBase64String`  |
| File                   | `EncryptedFile`          |

### Encryption

To encrypt data, call one of the factory methods on `Cryptography`, to
create a `EncryptionEngine` instance. The parameters to these factory 
methods require the correct combination of key and fixed salt for the 
chosen marker.

- `createKwc3EncryptionEngine(CryptographyKey.Password)`
- `createKwc5EncryptionEngine(CryptographyKey.Raw32)`
- `createFlexibleArgon2dEncryptionEngine(CryptographyKey.Password,
  CryptographyFixedSalt,
  CryptographyMarker.Flexible.KeyDerivation.Argon2d)`
- `createFlexibleArgon2iEncryptionEngine(CryptographyKey.Password,
  CryptographyFixedSalt,
  CryptographyMarker.Flexible.KeyDerivation.Argon2i)`
- `createFlexiblePbkdf2EncryptionEngine(CryptographyKey.Password,
  CryptographyFixedSalt,
  CryptographyMarker.Flexible.KeyDerivation.Pbkdf2)`
- `createFlexibleNoDerivationEncryptionEngine(CryptographyKey.Raw32)`
- `createFlexibleNoDerivation64EncryptionEngine(CryptographyKey.Raw64)`

Alternatively, the
`Cryptography.createEncryptionEngine(CryptographyMarker,
CryptographyKey, CryptographyFixedSalt?)` extension factory method can be 
used to create a generic encryption engine. This method throws a
`CryptographyEncryptionSettingsArgumentException` if the combination of
parameters is invalid.

The `EncryptionEngine` class provides a generic API which supports any
stream format for input and output, provided that the output respects
the contract for `EncryptionSink`, allowing to overwrite it from the
start.

```kotlin
fun encrypt(
    encryptionSink: EncryptionSink,
    compressed: Boolean = false
): Sink
```

Some utility methods are available to perform encryption for common
targets for the input and for the output, and for a generic input
written to a `Sink`.

Input
- Byte Array
- UTF8 String
- File
- _Other_ - Write `Sink`

Output
- Byte Array
- Base64
- File

The follow table provides an overview of the existing methods.

| Input \ Output | Byte Array                                                                                 | Base64                                                                                           | File                                                                          |
|:---------------|:-------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------|
| Byte Array     | `fun EncryptionEngine.encryptByteArrayToByteArray(ByteArray, Boolean): EncryptedByteArray` | `fun EncryptionEngine.encryptByteArrayToBase64String(ByteArray, Boolean): EncryptedBase64String` | _Not Implemented_                                                             |
| String (UTF8)  | `fun EncryptionEngine.encryptUtf8ToByteArray(String, Boolean): EncryptedByteArray`         | `fun EncryptionEngine.encryptUtf8ToBase64String(String, Boolean): EncryptedBase64String`         | `fun EncryptionEngine.encryptUtf8ToFile(String, EncryptedFile, Boolean)`      |
| File           | _Not Implemented_                                                                          | _Not Implemented_                                                                                | `fun EncryptionEngine.encryptFileToFile(File, EncryptedFile, Boolean)`        |
| _Other_        | `fun EncryptionEngine.encryptToByteArray(Boolean, (Sink) -> Unit)): EncryptedByteArray`    | `fun EncryptionEngine.encryptToBase64String(Boolean, (Sink) -> Unit)): EncryptedBase64String`    | `fun EncryptionEngine.encryptToFile(EncryptedFile, Boolean, (Sink) -> Unit))` |

### Decryption

To decrypt data, call the `createDecryptionEngine(CryptographyKey)`
factory method on `Cryptography`, to create a `DecryptionEngine`
instance. This method requires a cryptography key, and the decryption
will later fail if that key is incorrect for the markers detected in the
data provided for decryption.

The `DecryptionEngine` class provides a generic API which supports any
stream format for input and output, provided that the input respects the
contract for `DecryptionSource`, allowing it to be opened multiple
times.

```kotlin
fun <T> decrypt(
    decryptionSource: DecryptionSource,
    compressed: Boolean = false,
    process: (Source) -> T
): T
```

In the same way as the encryption engine, some utility methods are
available to perform decryption for common targets for the input and for
the output, and for a generic output read from a `Source`.

Input
- Byte Array
- Base64
- File

Output
- Byte Array
- UTF8 String
- File
- _Other_ - Read `Source`

The table summarising the existing methods is similar to the one for
encryption, flipping the columns and rows for input and output.

| Output \ Input | Byte Array                                                                                  | Base64                                                                                      | File                                                                              |
|:---------------|:--------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------|
| Byte Array     | `fun DecryptionEngine.decryptByteArrayToByteArray(EncryptedByteArray, Boolean): ByteArray`  | `fun DecryptionEngine.decryptBase64ToByteArray(EncryptedBase64String, Boolean): ByteArray`  | _Not Implemented_                                                                 |
| String (UTF8)  | `fun DecryptionEngine.decryptByteArrayToUtf8String(EncryptedByteArray, Boolean): String`    | `fun DecryptionEngine.decryptBase64ToUtf8String(EncryptedBase64String, Boolean): String`    | `fun DecryptionEngine.decryptFileToUtf8String(EncryptedFile, Boolean): String`    |
| File           | _Not Implemented_                                                                           | _Not Implemented_                                                                           | `fun DecryptionEngine.decryptFileToFile(EncryptedFile, File, Boolean)`            |
| _Other_        | `fun <T> DecryptionEngine.decryptByteArray(EncryptedByteArray, Boolean, (Source) -> T)): T` | `fun <T> DecryptionEngine.decryptBase64(EncryptedBase64String, Boolean, (Source) -> T)): T` | `fun <T> DecryptionEngine.decryptFile(EncryptedFile, Boolean, (Source) -> T)): T` |

Operations from byte array or string to byte array or string also have
their corresponding fail-safe variants, which don't require exceptions
to be caught.

| Output \ Input | Byte Array                                                                               | Base64                                                                           |
|:---------------|:-----------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------|
| Byte Array     | `fun DecryptionEngine.decryptByteArrayToByteArrayOrNull(EncryptedByteArray, Boolean): ByteArray?` | `fun DecryptionEngine.decryptBase64ToByteArrayOrNull(EncryptedBase64String, Boolean): String?`  |
| String (UTF8)  | `fun DecryptionEngine.decryptByteArrayToUtf8StringOrNull(EncryptedByteArray, Boolean): String?`   | `fun DecryptionEngine.decryptBase64ToUtf8StringOrNull(EncryptedBase64String, Boolean): String?` |

### Change Cryptography

To make the operation of changing the cryptography for encrypted content
simpler and more efficient, a dedicated
`CryptographyChanger` interface is provided. Construct an instance by 
providing the decryption and encryption engine to use for the process.

## Exceptions

All decryption methods can throw a `CryptographyException`. The actual
subclass of `CryptographyException` which is thrown depends on the error
encountered, however due to the complexity of the cryptographic process
it may be difficult to determine the actual cause. For example, a wrong
password should throw a `CryptographyCipherException` when using the
legacy KWC3 marker, but it should throw a
`CryptographyChecksumException` when using a flexible header. For this
reason, it's generally simpler a generic `CryptographyException` when
calling the decryption methods. If the decryption engine managed to
successfully identify the marker in the data being decrypted, then this
marker value is included in the exception. The actual details of the
exception are mostly useful when debugging issues.

Exceptions thrown while reading the data to be processed or while
treating the processed data are propagated as-is by the encryption and
decryption methods. This includes for example `IOException`s thrown
while reading or writing files, which may be thrown by the corresponding
methods which deal with files for the encrypted or the decrypted data.
Likewise, if for example the process to treat decrypted data performs
some XML or JSON parsing, then exceptions thrown by these parsers will
be propagated and thrown directly by the decryption method used.

## Artifacts

The main [cryptography](cryptography) module is a Kotlin Multiplatform
project, configured only for the JVM platform. It publishes the
`com.dashlane:cryptography` artifact.

At runtime, projects should also depend either on the
`com.dashlane:cryptography-android` artifact, produced by the
[cryptography-android](cryptography-android) module, or on the
`com.dashlane:cryptography-jdk` artifact, produced by the
[cryptography-jdk](cryptography-jdk) module. Both of these depend on the
main `cryptography` artifact, and their role is to provide an
implementation of the key derivation methods for each JVM platform. The
`cryptography-android` module provides a battle-tested JNI
implementation which is optimized for Android. The `cryptography-jdk`
implementation is more portable. In Android library projects, the
recommended setup is therefore for the default configuration to depend
on `com.dashlane:cryptography`, the unit test configuration to depend on
`com.dashlane:cryptography-jdk`, and the instrumented test configuration
to depend on `com.dashlane:cryptography-android`. The app which
integrates the library needs to depend on the
`com.dashlane:cryptography-android` artifact.

The following shows an example of an Android library configuration.

```kotlin
dependencies {
    implementation("com.dashlane:cryptography:${Versions.DASHLANE_CRYPTOGRAPHY}")
    testImplementation("com.dashlane:cryptography-jdk:${Versions.DASHLANE_CRYPTOGRAPHY}")
    androidTestImplementation("com.dashlane:cryptography-android${Versions.DASHLANE_CRYPTOGRAPHY}")
}
```

# Module cryptography-android

Android implementation of primitives for symmetric cryptography.

See the `cryptography` module documentation.

# Module cryptography-app

Demo Android application for cryptographic operations.

# Module cryptography-cli

Command-line interface for cryptographic operations.

To build and install the CLI in the `build` directory, run the `installDist` 
Gradle task.

```shell script
./gradlew :cryptography-cli:installDist
```

The command-line script can then be found at the path
`cryptography-cli/build/install/cryptography-cli/bin/cryptography-cli`. A 
`cryptography-cli.sh` symlink is available at the root of the
project to make usage easier. See examples below.

```shell script
#
# Print CLI help and documentation.
#
./cryptography-cli.sh --help

#
# Print CLI help and documentation for `encrypt` command.
#
./cryptography-cli.sh encrypt --help

#
# Print CLI help and documentation for `encrypt` command.
#
./cryptography-cli.sh decrypt --help

#
# Encrypt "Hello World!" using "Dashlane12" password and print the 
# result base 64 encoded to the terminal.
#
echo 'Hello world!' | ./cryptography-cli.sh encrypt --password=Dashlane12 - | base64

#
# Base64 decode the provided string then decrypt the data using 
# "Dashlane12" password, which prints the "Hello World!" result to the
# terminal. 
#
echo 'JDEkYXJnb24yZCQxNiQzJDMyNzY4JDIkYWVzMjU2JGNiY2htYWMkMTYk2Rxp/VARLTn6d+qkvTScR1/PDz59hGqpvkj2nmupQlk6MojLB4FVsH6mb4CnOmwqXF4nHV9xi5XAwNbNQHUFcSvhG3YaWSGopRZJjnl9z30=' | base64 -d | ./cryptography-cli.sh decrypt --password=Dashlane12 -

#
# Encrypt file `image.jpg` using "Dashlane12" password into file 
# `image.jpg.aes`.
#
./cryptography-cli.sh encrypt --password=Dashlane12 image.jpg > image.jpg.aes

#
# 
# Decrypt file `image.jpg.aes` using "Dashlane12" password into file 
# `image.jpg`.
#
./cryptography-cli.sh decrypt --password=Dashlane12 image.jpg.aes > image.jpg
```

# Module cryptography-core

Core cryptography model and utilities.

## Key

The cryptography key is the user secret used to encrypt & decrypt data.
It's either a UTF-8 encoded password, or a randomly generated sequence
of bytes.

The `CryptographyKey` class holds the bytes for a cryptographic key.

## Sharing Keys

The asymmetric key pairs are held together in a
`SharingKeys` instance, each key represented respectively by the type
`SharingKeys.Public` and `SharingKeys.Private`. These types are
currently just holders for the PEM representations of the keys, and
invalid formats lead to failures when doing operations using
`SharingCryptography`. Ideally, the failure should occur when building
the keys.

# Module cryptography-jdk

JDK implementation of primitives for symmetric cryptography.

See the `cryptography` module documentation.

# Module cryptography-sharing

Asymmetric cryptography operations. Currently, these all use rely on the RSA 
algorithm.

See the `SharingCryptography` interface for the API with the various 
operations. Extension methods are available to perform operations using 
base64 encoded data and ignoring cryptographic errors.

There are two types of schemas for sharing encrypted data, corresponding
to data which is encrypted using RSA and to data containing an RSA
signature. For these two different semantics, different data types are
available, corresponding to different data formats.

| Format                 | RSA Encrypted Data               | RSA Signature Data                |
|:-----------------------|:---------------------------------|:----------------------------------|
| Byte Array             | `SharingEncryptedByteArray`      | `SharingSignatureByteArray`       |
| Base 64 Encoded String | `SharingEncryptedBase64String`   | `SharingSignatureBase64String`    |

The `cryptography-sharing` artifact provides the API for sharing
cryptography, used for any Java/Kotlin module. To provide an
implementation for the runtime, the `cryptography-sharing-android`
artifact provides the Android-optimized implementation using the JNI
while the `cryptography-sharing-jdk` provides the generic JDK
implementation.

# Module cryptography-sharing-android

Android implementation of primitives for asymmetric cryptography.

See the `cryptography-sharing` module documentation.

# Module cryptography-sharing-jdk

JDK implementation of primitives for asymmetric cryptography.

See the `cryptography-sharing` module documentation.

# Module cryptography-xml

Integration module for symmetric cryptography to facilitate encryption and 
decryption operations for XML content. 

See the `cryptography` module documentation for the description of the core 
cryptographic engine.

Use the `forXml` extension method on `EncryptionEngine` and 
`DecryptionEngine` to respectively create `XmlEncryptionEngine` and
`XmlDecryptionEngine` instances, which combine the serialization and
cryptography capabilities for XML transactions and backups.

```kotlin
fun EncryptionEngine.forXml(serializer: XmlSerializer): XmlEncryptionEngine
```

```kotlin
fun DecryptionEngine.forXml(deserializer: XmlDeserializer): XmlDecryptionEngine
```

## XML Encryption

The `XmlEncryptionEngine` interface provides methods for serializing and
encrypting an XML transaction or backup.

```kotlin
fun encryptXmlTransaction(
    xml: XmlTransaction,
    encryptionSink: EncryptionSink,
    compressed: Boolean = true
)
```

```kotlin
fun encryptXmlBackup(
    xml: XmlBackup,
    encryptionSink: EncryptionSink,
    compressed: Boolean = true
)
```

Extensions are available to support encryption to a base 64 encoded
string or to a file.

| Input \ Output  | Base64                                                                                                        | File                                                                                          |
|:----------------|:--------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------|
| XML Transaction | `fun XmlEncryptionEngine.encryptXmlTransactionToBase64String(XmlTransaction, Boolean): EncryptedBase64String` | `fun XmlEncryptionEngine.encryptXmlTransactionToFile(XmlTransaction, EncryptedFile, Boolean)` |
| XML Backup      | `fun XmlEncryptionEngine.encryptXmlBackupToBase64String(XmlBackup, Boolean): EncryptedBase64String`           | `fun XmlEncryptionEngine.encryptXmlBackupToFile(XmlBackup, EncryptedFile, Boolean)`           |

## XML Decryption

The `XmlDecryptionEngine` interface provides methods for decrypting and
deserializing an XML transaction or backup.

```kotlin
fun decryptToXmlTransaction(
    decryptionSource: DecryptionSource,
    compressed: Boolean = true,
): XmlTransaction
```

```kotlin
fun decryptToXmlBackup(
    decryptionSource: DecryptionSource,
    compressed: Boolean = true,
): XmlBackup
```

Extensions are available to support decryption from a base 64 encoded
string or from a file.

| Output \ Input  | Base64                                                                                                  | File                                                                                          |
|:----------------|:--------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------|
| XML Transaction | `fun XmlDecryptionEngine.decryptBase64ToXmlTransaction(EncryptedBase64String, Boolean): XmlTransaction` | `fun XmlDecryptionEngine.decryptFileToXmlTransaction(EncryptedFile, Boolean): XmlTransaction` |
| XML Backup      | `fun XmlDecryptionEngine.decryptBase64ToXmlBackup(EncryptedBase64String, Boolean): XmlBackup`           | `fun XmlDecryptionEngine.decryptFileToXmlBackup(EncryptedFile, Boolean): XmlBackup`           |

The decryption methods for base 64 also have their fail-safe variants
which ignore both decryption and XML deserialization errors.

| Output \ Input  | Base64                                                                                                        |
|:----------------|:--------------------------------------------------------------------------------------------------------------|
| XML Transaction | `fun XmlDecryptionEngine.decryptBase64ToXmlTransactionOrNull(EncryptedBase64String, Boolean): XmlTransaction` |
| XML Backup      | `fun XmlDecryptionEngine.decryptBase64ToXmlBackupOrNull(EncryptedBase64String, Boolean): XmlBackup`           |

