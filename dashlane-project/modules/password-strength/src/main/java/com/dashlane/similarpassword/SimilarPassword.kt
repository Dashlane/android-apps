package com.dashlane.similarpassword

import javax.inject.Inject

data class SimilarPassword constructor(
    private val minCharsBeforeCheckSimilarity: Int,
    private val similarLevenshteinThreshold: Int
) {
    private val distanceCalculator = LevenshteinDistance()

    @Inject
    constructor() : this(minCharsBeforeCheckSimilarity = 5, similarLevenshteinThreshold = 3)

    fun areSimilar(password1: String?, password2: String?): Boolean {
        if (password1 == password2) {
            return true 
        }
        if (password1 == null || password2 == null) return false 
        if (isShortPassword(password1) || isShortPassword(password2)) {
            return false 
        }
        
        return distanceCalculator.limitedCompare(password1, password2, similarLevenshteinThreshold) != -1
    }

    fun isShortPassword(password: String) = password.length < minCharsBeforeCheckSimilarity
}