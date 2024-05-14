package com.dashlane.storage.userdata.accessor.filter

fun credentialFilter(init: CredentialFilter.() -> Unit): CredentialFilter {
    val filter = CredentialFilter()
    filter.init()
    return filter
}

fun genericFilter(init: GenericFilter.() -> Unit): GenericFilter {
    val filter = GenericFilter()
    filter.init()
    return filter
}

fun counterFilter(init: CounterFilter.() -> Unit): CounterFilter {
    val filter = CounterFilter()
    filter.init()
    return filter
}

fun vaultFilter(init: VaultFilter.() -> Unit): VaultFilter {
    val filter = VaultFilter()
    filter.init()
    return filter
}

fun collectionFilter(init: CollectionFilter.() -> Unit): CollectionFilter {
    val filter = CollectionFilter()
    filter.init()
    return filter
}