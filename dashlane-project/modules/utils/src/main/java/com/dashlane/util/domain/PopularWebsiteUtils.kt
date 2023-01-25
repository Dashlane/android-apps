package com.dashlane.util.domain

import android.content.Context
import android.content.pm.PackageManager
import com.dashlane.ext.application.KnownApplication
import com.dashlane.util.getInstalledApplicationCompat

object PopularWebsiteUtils {

    @JvmStatic
    fun getPopularWebsites(context: Context): List<String> {
        val allPopularApplications = KnownApplication.allPopularApplications.map { it.mainDomain }
        val installedPackageNamesWebsites =
            context.packageManager.getInstalledApplicationCompat(PackageManager.GET_META_DATA)
                .mapNotNull { KnownApplication.getKnownApplication(it.packageName)?.mainDomain }

        val allWebsites = (allPopularApplications + installedPackageNamesWebsites).distinct()

        return allWebsites.sortedWith(
            compareBy(
                
                { !installedPackageNamesWebsites.contains(it) },
                
                { it })
        )
    }
}