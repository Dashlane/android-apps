package com.dashlane.passwordanalysis

open class GroupOfAuthentifiant<out T>(
    val groupBy: T,
    val authentifiants: List<AnalyzedAuthentifiant>,
    val countReal: Int = authentifiants.size,
)