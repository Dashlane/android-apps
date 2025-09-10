# Contributing 

## Publishing

To generate a new version of the Dashlane Autofill Form Detector artifact, 
commit the changes to a new branch, push to the server, and open a pull 
request.
Make sure that the CHANGELOG is updated with the pending version number
and the list of changes. 

After the pull request is merged, tag the merge
commit with the code of the new version, for example `1.2.3`. The
published tag should respect [semantic versioning](https://semver.org/)
and be consistent with previous tags. The CI handles the publication of
a new version whenever a tag is pushed. The dependency can then be
updated in projects which consume the API.

During development, it can be useful to publish the artifact to the
local maven repository. Use the gradlew `./gradlew publishToMavenLocal`
to publish a new local development version (which will overwrite the
previously published development version, if any). In that case, the
version of the artifact is left at its default value when not on a
tagged commit, `unspecified`. Projects can depend on this version, to
validate it before publishing a new version to the remote repository.
