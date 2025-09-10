package com.dashlane.autofill.util

import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

internal fun calculateEntriesDistance(
    referenceTree: List<String>,
    candidateTree: List<String>,
): Int? {
    if (referenceTree.isEmpty() || candidateTree.isEmpty()) {
        return null
    }
    val lcaIndex = findLcaIndex(reference = referenceTree, candidate = candidateTree)
        ?: return null 

    
    return (referenceTree.lastIndex - lcaIndex) + (candidateTree.lastIndex - lcaIndex)
}

internal fun findLcaIndex(
    reference: List<String>,
    candidate: List<String>,
): Int? {
    val diffIndex = reference
        .zip(candidate)
        .indexOfFirst { it.first != it.second }
        .takeUnless {
            
            it == -1
        }
        ?: minOf(reference.size, candidate.size)

    return if (diffIndex == 0) {
        
        null
    } else {
        diffIndex - 1
    }
}