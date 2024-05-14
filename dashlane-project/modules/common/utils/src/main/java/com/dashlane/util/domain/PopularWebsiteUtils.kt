package com.dashlane.util.domain

import android.content.Context
import android.content.pm.PackageManager
import com.dashlane.ext.application.KnownApplicationProvider
import com.dashlane.util.getInstalledApplicationCompat

object PopularWebsiteUtils {

    fun getPopularWebsites(context: Context, knownApplicationProvider: KnownApplicationProvider): List<String> {
        val allPopularApplications = knownApplicationProvider.popularServices
            .sortedByDescending { it.count }
            .map { it.domain }
            .distinctBy { it.split(".").first() }
            .take(NUMBER_OF_SUGGESTION)
        val installedPackageNamesWebsites =
            context.packageManager.getInstalledApplicationCompat(PackageManager.GET_META_DATA)
                .mapNotNull {
                    knownApplicationProvider.getKnownApplication(it.packageName)?.mainDomain
                }

        val allWebsites = (allPopularApplications + installedPackageNamesWebsites).distinct()

        return allWebsites
            .filterNot { it == "dashlane.com" }
            .sortedWith(
                compareBy(
                    
                    { !installedPackageNamesWebsites.contains(it) },
                    
                    { it }
                )
            )
    }

    private const val NUMBER_OF_SUGGESTION = 50
}