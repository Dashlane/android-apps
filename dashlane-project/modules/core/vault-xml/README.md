# Vault XML

## Vault XML Data

The `vault-xml` project contains the model which is used to represent
the semantics of the recursive XML data structures contained in the user
vault data, as represented by `XmlData` held by `XmlTransaction` or
`XmlBackup`.

## Vault XML Domain

The `vault-xml-domain` contains the generated `SyncObject` wrappers
which provide accessors to the properties of XML structures for
different data types within the user vault, accompanied by the
`SyncObjectType` matching enum which provides additional properties for
each `SyncObject` handled during the sync.

The dependency on the `vault-xml` module is marked as `implementation`
because projects should be able to use `SyncObject`s without any
knowledge of the underlying `XmlData`.

## Vault XML Generator

The `vault-xml-domain-generator` uses the platform-agnostic
specification of the
[Personal Data Model](https://gitlab.dashlane.com/dashlaneplatform/personal-data-model)
to generate a the data structures used by the `vault-xml-domain`
project.

## Vault XML Serializer

The `vault-xml-serializer` handles the serialization and deserialization
of the data structures from the `vault-xml` project into proper XML.

## Vault XML Serializer Tests

The `vault-xml-serializer-test` project contains instrumented tests used
to validate the implementation in `vault-xml-serializer`. It's important
to run these tests on the Android platform because the underlying XML
transformer implementation is provided opaquely by the Java standard
library and is therefore dependent on the JVM implementation used.
