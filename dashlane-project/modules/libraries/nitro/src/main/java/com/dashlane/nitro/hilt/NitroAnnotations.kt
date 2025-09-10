package com.dashlane.nitro.hilt

import androidx.annotation.Discouraged
import javax.inject.Qualifier

@Qualifier
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
)
@Discouraged(
    "Note Q1 2025: this flavour is expected to be deprecated in favour of the Nodews Enclave Proxy, and all " +
        "uses will progressively be migrated to the proxy's API.\n" +
        "https://dashlane.gitlab.dashlane.com/teams/code/server/misc/api-documentation-v2/category/nodews-enclave-proxy",
)
annotation class NitroLegacy

@Qualifier
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class NitroProxy