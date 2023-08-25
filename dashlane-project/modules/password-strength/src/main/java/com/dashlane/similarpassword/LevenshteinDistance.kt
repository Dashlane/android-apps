package com.dashlane.similarpassword

import java.util.Arrays

class LevenshteinDistance {
    
    private val pArray = IntArray(DEFAULT_MAX_STRING_SIZE)
    private val dArray = IntArray(DEFAULT_MAX_STRING_SIZE)

    
    @Suppress("kotlin:S3776", "LongMethod", "ComplexMethod")
    fun limitedCompare(left: CharSequence?, right: CharSequence?, threshold: Int): Int { 
        var tmpLeft = left
        var tmpRight = right
        if (tmpLeft == null || tmpRight == null) {
            throw IllegalArgumentException("Strings must not be null")
        }
        if (threshold < 0) {
            throw IllegalArgumentException("Threshold must not be negative")
        }


        var n = tmpLeft.length 
        var m = tmpRight.length 

        
        
        if (n == 0) {
            return if (m <= threshold) m else -1
        } else if (m == 0) {
            return if (n <= threshold) n else -1
        }

        if (n > m) {
            
            val tmp = tmpLeft
            tmpLeft = tmpRight
            tmpRight = tmp
            n = m
            m = tmpRight.length
        }
        var p = pArray 
        var d = dArray 
        if (n + 1 > DEFAULT_MAX_STRING_SIZE) {
            p = IntArray(n + 1)
            d = IntArray(n + 1)
        }
        var tempD: IntArray 

        
        val boundary = Math.min(n, threshold) + 1
        for (i in 0 until boundary) {
            p[i] = i
        }
        
        
        Arrays.fill(p, boundary, p.size, Integer.MAX_VALUE)
        Arrays.fill(d, Integer.MAX_VALUE)

        
        for (j in 1..m) {
            val rightJ = tmpRight[j - 1] 
            d[0] = j

            
            val min = Math.max(1, j - threshold)
            val max = if (j > Integer.MAX_VALUE - threshold) {
                n
            } else {
                Math.min(
                    n,
                    j + threshold
                )
            }

            
            
            if (min > max) {
                return -1
            }

            
            if (min > 1) {
                d[min - 1] = Integer.MAX_VALUE
            }

            
            for (i in min..max) {
                if (tmpLeft[i - 1] == rightJ) {
                    
                    d[i] = p[i - 1]
                } else {
                    
                    
                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1])
                }
            }

            
            tempD = p
            p = d
            d = tempD
        }

        
        
        
        return if (p[n] <= threshold) {
            p[n]
        } else {
            -1
        }
    }

    companion object {
        private const val DEFAULT_MAX_STRING_SIZE = 30
    }
}
