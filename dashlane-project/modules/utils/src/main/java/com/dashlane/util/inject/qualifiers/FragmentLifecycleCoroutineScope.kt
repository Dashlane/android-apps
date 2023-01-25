package com.dashlane.util.inject.qualifiers

import javax.inject.Qualifier



@MustBeDocumented
@Qualifier
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class FragmentLifecycleCoroutineScope
