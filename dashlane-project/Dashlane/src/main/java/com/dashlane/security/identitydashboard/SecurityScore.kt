package com.dashlane.security.identitydashboard

import com.dashlane.security.identitydashboard.SecurityScore.Companion.D
import com.dashlane.security.identitydashboard.SecurityScore.Companion.X
import kotlin.math.pow



class SecurityScore {

    companion object {
        private const val MIN_SCORE = 0.2f
        private const val X = 5
        private const val M = 1.0f
        private const val N = 1.0f
        private const val D = 0.6f

        

        fun getSecurityScore(
            allCorrupted: Int,
            importantCorrupted: Int,
            accountCount: Int
        ): Float {
            if (accountCount < X) {
                return -1.0f
            }

            val important = D * (1.0f - importantCorrupted.toFloat() / accountCount.toFloat()).pow(N)
            val base = (1.0f - D) * (1.0f - allCorrupted.toFloat() / accountCount.toFloat()).pow(M)
            return MIN_SCORE + (1 - MIN_SCORE) * (important + base)
        }
    }
}