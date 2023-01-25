package com.dashlane.login.dagger

import javax.inject.Qualifier



@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Qualifier
annotation class ShortTimeout
