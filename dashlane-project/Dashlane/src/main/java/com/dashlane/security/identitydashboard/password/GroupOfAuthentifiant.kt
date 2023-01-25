package com.dashlane.security.identitydashboard.password

open class GroupOfAuthentifiant<out T>(
    val groupBy: T,
    val authentifiants: List<AnalyzedAuthentifiant>,
    val countReal: Int = authentifiants.size
)