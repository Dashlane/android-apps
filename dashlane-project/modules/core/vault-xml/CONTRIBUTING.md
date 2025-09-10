# Contributing

## Generate the vault-xml-domain

To update the vault-xml-domain properties first verify what is the latest version of 
[the personal data model](https://gitlab.dashlane.com/dashlaneplatform/personal-data-model) and update the dependency in this project.
Then you can generate the new version of the vault-xml-domain using the this command:

`./gradlew modules:core:vault-xml:vault-xml-domain-generator:run`
