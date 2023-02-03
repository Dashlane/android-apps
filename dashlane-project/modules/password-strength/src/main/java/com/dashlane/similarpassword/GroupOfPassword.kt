package com.dashlane.similarpassword

import java.util.BitSet
import java.util.Comparator



data class GroupOfPassword(
    val initialPassword: String,
    val passwords: Set<String>
) {

    val size = passwords.size

    fun contains(password: String?) = passwords.contains(password)

    fun contains(other: GroupOfPassword): Boolean {
        val otherPasswords = other.passwords
        if (otherPasswords == passwords) {
            return true 
        }
        if (otherPasswords.size > passwords.size) {
            return false 
        }
        return otherPasswords.all { passwords.contains(it) }
    }

    class Builder(private val similarPassword: SimilarPassword = SimilarPassword()) {

        fun compute(passwords: Array<String>): List<GroupOfPassword> {
            passwords.sort() 

            lastComputedListPasswords?.takeIf { lastComputedSimilarPassword == similarPassword }?.let {
                if (passwords.contentEquals(it)) {
                    
                    lastComputedResult?.let { return it }
                }
            }

            val passwordsDistinct = removeDuplicateAndSmall(passwords)

            val similarPasswordMap = getPasswordSimilarityMap(passwordsDistinct)

            
            val sortedBitSets =
                similarPasswordMap.sortedWith(Comparator { p0, p1 -> p1.cardinality() - p0.cardinality() })

            val toKeep = getPasswordsToKeep(sortedBitSets)

            val groupsSimilar = createGroupsSimilar(similarPasswordMap, sortedBitSets, toKeep, passwordsDistinct)
            val groupsSimilarWithoutUnique = groupsSimilar.filter { groupOfPassword ->
                
                groupOfPassword.size > 1 || passwords.count { groupOfPassword.initialPassword == it } > 1
            }

            val shortPasswordsDuplicate = getShortPasswordsDuplicate(passwords)
            val result = if (shortPasswordsDuplicate.isEmpty()) {
                groupsSimilarWithoutUnique
            } else {
                groupsSimilarWithoutUnique.plus(shortPasswordsDuplicate.map { GroupOfPassword(it, setOf(it)) })
            }
            lastComputedSimilarPassword = similarPassword
            lastComputedResult = result
            lastComputedListPasswords = passwords

            return result
        }

        private fun getShortPasswordsDuplicate(passwords: Array<String>): Set<String> {
            val shortPasswords = passwords.filter { similarPassword.isShortPassword(it) }
            if (shortPasswords.size <= 1) {
                return setOf()
            }

            val duplicatePasswords = shortPasswords.mapIndexedNotNull { index, password ->
                
                val duplicate = ((index + 1) until shortPasswords.size).any { password == shortPasswords[it] }
                if (duplicate) {
                    password
                } else {
                    null
                }
            }
            return duplicatePasswords.toSet()
        }

        

        private fun getPasswordsToKeep(sortedBitSets: List<BitSet>): BitSet {
            val totalCount = sortedBitSets.size
            val toKeep = BitSet(totalCount)
            toKeep.set(0, totalCount)
            for (i in 0 until totalCount) {
                if (toKeep.get(i)) {
                    for (j in i + 1 until totalCount) {
                        val newBitSet = BitSet(totalCount)
                        newBitSet.or(sortedBitSets[i])
                        newBitSet.flip(0, totalCount)
                        newBitSet.and(sortedBitSets[j])
                        if (newBitSet.cardinality() == 0) {
                            
                            toKeep.clear(j)
                        }
                    }
                }
            }
            return toKeep
        }

        

        private fun removeDuplicateAndSmall(passwords: Array<String>) =
            passwords.distinct().filterNot { similarPassword.isShortPassword(it) }

        

        private fun getPasswordSimilarityMap(passwords: List<String>): Array<BitSet> {
            val totalCount = passwords.size
            val bitSets = Array(totalCount, { BitSet(totalCount) })

            for (i in passwords.indices) {
                val password1 = passwords[i]
                bitSets[i].set(i)
                for (j in i + 1 until totalCount) {
                    val password2 = passwords[j]

                    if (similarPassword.areSimilar(password1, password2)) {
                        
                        bitSets[i].set(j)
                        bitSets[j].set(i)
                    }
                }
            }
            return bitSets
        }

        

        private fun createGroupsSimilar(
            originalPasswordMap: Array<BitSet>,
            sortedBitSets: List<BitSet>,
            toKeep: BitSet,
            passwords: List<String>
        ): List<GroupOfPassword> {
            val totalCount = sortedBitSets.size
            val allGroups = mutableListOf<GroupOfPassword>()
            for (i in 0 until totalCount) {
                if (toKeep.get(i)) {
                    val bitSet = sortedBitSets[i]
                    val setOfPasswords = mutableSetOf<String>()

                    val indexOfOriginalPassword = originalPasswordMap.indexOf(bitSet)
                    val originalPassword = passwords[indexOfOriginalPassword]

                    for (j in 0 until totalCount) {
                        if (bitSet.get(j)) {
                            setOfPasswords.add(passwords[j])
                        }
                    }
                    val groupOfPasswords = GroupOfPassword(originalPassword, setOfPasswords.toSet())
                    allGroups.add(groupOfPasswords)
                }
            }
            return allGroups.toList()
        }

        companion object {
            private var lastComputedSimilarPassword: SimilarPassword? = null
            private var lastComputedListPasswords: Array<String>? = null
            private var lastComputedResult: List<GroupOfPassword>? = null
        }
    }
}
