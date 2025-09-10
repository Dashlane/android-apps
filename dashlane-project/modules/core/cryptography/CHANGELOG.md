
Changes
=======

##  Version 2.7.2

Change: Bump dependencies

##  Version 2.7.1

Change: SDK upgrade from 33 to 35

##  Version 2.7.0

Change: Bump dependencies

##  Version 2.6.0

Change: Update vault-xml to 4.10.0

##  Version 2.5.2

Change: fix signature for artifact

##  Version 2.5.1

New: Add signature for artifact

##  Version 2.5.0

Change: switch to gitlab maven registry

##  Version 2.4.0

New: `fun EncryptedFile.asEncryptionSink` now has a `fsyncOnClose` parameter.

Change: `EncryptionSink` implements `Closeable`. This is necessary to 
implement the `fsync` for files. This change requires that all code which 
use a `EncryptionSink` is updated to call `use` on it.

##  Version 2.3.0

New: `XmlArchive` for secure archive parsing and serializing. 

##  Version 2.2.1

Fix: Fix leaking of open files. `RandomAccessFile` was not being closed 
correctly with its file descriptor, so use its channel instead.

##  Version 2.2.0

Change: Update cryptography JNI to 2.0.0.

##  Version 2.1.1

Fix: `CryptographyKey.Bytes32` now also supports 16-byte or 64-byte keys.

##  Version 2.1.0

Add `CryptographyKey.copy` method.

Add `CryptographyAppKeyStore` and `CryptographyAppKeyProvider` for
Android.

Expose `ByteArray.use` and `ByteArray.close` methods as part of public
API.

Add `ObfuscatedByteArray` and extract `cryptography-core` module which
integrates obfuscation with the model for cryptography keys. Add the
cryptography keys and sharing keys model to this module. Also add UTF-8
and Base64 decoding and encoding capabilities to this module. Review
exception structure to use base `CryptographyException` from this
package, meaning that `SharingCryptographyException` was removed.

##  Version 2.0.1

Fix key derivation cache usage by decryption engine.

##  Version 2.0.0

Add legacy app decryption support for Android.

Add sharing cryptography support.

Add `CryptographyChanger` helper class to decrypt and re-encrypt.

Add `CryptographyComponent` and `SharingCryptographyComponent`
interfaces to use for composition with cryptography in dependency
injection.

Add types for encrypted data.

| Encryption    | Byte Array                  | Base64 String                  | File            |
|:--------------|:----------------------------|:-------------------------------|:----------------|
| AES           | `EncryptedByteArray`        | `EncryptedBase64String`        | `EncryptedFile` |
| RSA           | `SharingEncryptedByteArray` | `SharingEncryptedBase64String` |                 |
| RSA Signature | `SharingSignatureByteArray` | `SharingSignatureBase64String` |                 |

Introduced cryptography key type safety, so that an error is thrown if
the key type provided during encryption or decryption doesn't match the
one required by the cryptography marker.

Decoding the cryptography marker from streamed data is no longer part of
the public API. Instead, the marker can now be decoded from the
encrypted data types, which is safer and more convenient to use, using
the `EncryptedByteArray.getCryptographyMarker` and
`EncryptedBase64String.getCryptographyMarker` extension methods.

Use SAM interfaces for generators, to make them easier to implement with
lambdas.

##  Version 1.0.1

Configure compilation to create static `CryptographyKey` factory
methods, to be easier to call from Java.

##  Version 1.0.0

Initial version, supporting symmetric encryption and decryption for the
JVM platform.
