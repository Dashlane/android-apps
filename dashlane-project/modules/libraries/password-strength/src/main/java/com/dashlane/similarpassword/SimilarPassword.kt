package com.dashlane.similarpassword

data class SimilarPassword(
    private val minCharsBeforeCheckSimilarity: Int = 5,
    private val similarLevenshteinThreshold: Int = 3
) {
    private val distanceCalculator = LevenshteinDistance()

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