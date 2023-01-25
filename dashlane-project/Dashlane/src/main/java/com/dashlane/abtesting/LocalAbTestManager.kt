package com.dashlane.abtesting

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.dashlane.useractivity.log.install.InstallLogCode75
import com.dashlane.useractivity.log.install.InstallLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.util.Random
import javax.inject.Inject



class LocalAbTestManager constructor(
    private val sharedPreferences: SharedPreferences,
    private val installLogCodeRepository: InstallLogRepository,
    private val random: Random = SecureRandom()
) {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        installLogCodeRepository: InstallLogRepository
    ) : this(
        context.getSharedPreferences(SHARED_PREFERENCES_PREFIX, Context.MODE_PRIVATE),
        installLogCodeRepository
    )

    

    fun getVariant(localAbTest: LocalAbTest): Variant {
        return getVariant(localAbTest.testName, localAbTest.variants)
    }

    

    fun getStoredVariant(localAbTest: LocalAbTest): Variant? {
        return getStoredVariant(localAbTest.testName, localAbTest.variants)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getVariant(testName: String, variants: Map<Variant, Int>): Variant =
        
        getStoredVariant(testName, variants) ?: randomVariant(variants).also {
            
            storeVariant(testName, it)
        }

    fun getSelectionDate(test: LocalAbTest): Long? {
        return sharedPreferences.getLong("${test.testName}$SELECTION_DATE_SUFFIX", System.currentTimeMillis())
    }

    

    fun assignAndLogExperiment(abTest: LocalAbTest) {
        val variant = getVariant(abTest)
        logExperiment(abTest, variant)
    }

    private fun logExperiment(abTest: LocalAbTest, variant: Variant) {
        installLogCodeRepository.enqueue(
            InstallLogCode75(
                experimentId = abTest.testName.lowercase(),
                variantId = variant.name
            ),
            true
        )
    }

    @Suppress("SharedPreferencesSecurity")
    private fun storeVariant(testName: String, variant: Variant) {
        sharedPreferences.edit()
            .putString(testName, variant.name)
            .putLong("$testName$SELECTION_DATE_SUFFIX", System.currentTimeMillis())
            .apply()
    }

    private fun getStoredVariant(testName: String, variants: Map<Variant, Int>) =
        sharedPreferences.getString(testName, null)?.let { variantName ->
            variants.keys.firstOrNull { it.name == variantName }
        }

    private fun randomVariant(variants: Map<Variant, Int>): Variant {
        var maxWeight = 0
        val randomPercent = random.nextInt(variants.values.sumOf { it })

        for (variant in variants) {
            maxWeight += variant.value
            if (randomPercent < maxWeight) {
                return variant.key
            }
        }

        
        return variants.keys.last()
    }

    companion object {
        const val SHARED_PREFERENCES_PREFIX = "ab_test"
        const val SELECTION_DATE_SUFFIX = "_selection_date_millis"
    }
}