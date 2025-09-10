package com.dashlane.sync.domain

import javax.inject.Qualifier

@MustBeDocumented
@Qualifier
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class SupportedTransactionsFilter