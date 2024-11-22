package com.dashlane.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.dashlane.core.helpers.AppSignature
import com.dashlane.ext.application.AutofillExtraDataApplication
import com.dashlane.url.isTrademarkDomain
import com.dashlane.url.toUrlOrNull
import okio.ByteString

object PackageUtilities {
    private val DOMAINS_EXTENSIONS = arrayOf(
        "aero", "asia", "biz", "cat", "com", "coop", "info", "int", "jobs", "mobi", "museum", "name",
        "net", "org", "post", "pro", "tel", "travel", "xxx", "edu", "gov", "mil", "ac", "ad", "ae", "af",
        "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "as", "at", "au", "aw", "ax", "az", "ba", "bb",
        "bd", "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by",
        "bz", "ca", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cr", "cs", "cu",
        "cv", "cx", "cy", "cz", "dd", "de", "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg", "eh", "er",
        "es", "et", "eu", "fi", "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh",
        "gi", "gl", "gm", "gn", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr",
        "ht", "hu", "id", "ie", "il", "im", "in", "io", "iq", "ir", "is", "it", "je", "jm", "jo", "jp",
        "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk",
        "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md", "me", "mg", "mh", "mk", "ml", "mm", "mn",
        "mo", "mp", "mq", "mr", "ms", "mt", "mu", "mv", "mw", "mx", "my", "mz", "na", "nc", "ne", "nf",
        "ng", "ni", "nl", "no", "np", "nr", "nu", "nz", "om", "pa", "pe", "pf", "pg", "ph", "pk", "pl",
        "pm", "pn", "pr", "ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa", "sb", "sc",
        "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "ss", "st", "su", "sv",
        "sx", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to", "tp", "tr",
        "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn",
        "vu", "wf", "ws", "ye", "yt", "yu", "za", "zm"
    )
    private val APP_KEYWORD_TO_IGNORE = arrayOf(
        "app", "android", "droid", "full", "free", "mobile", "client", "mail", "sig", "pass", "password", "secure", "direct", "connect"
    ).plus(DOMAINS_EXTENSIONS)

    private val cacheKeywords = HashMap<String, List<String>>()

    fun getApplicationNameFromPackage(context: Context, packageName: String): String? {
        val pm = context.packageManager
        return try {
            pm.getApplicationInfoCompat(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }?.let { pm.getApplicationLabel(it).toString() }
    }

    fun getPackageInfoFromPackage(context: Context, packageName: String): PackageInfo? {
        val pm = context.packageManager
        return try {
            pm.getPackageInfoCompat(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun getKeywords(context: Context, packageName: String): List<String> {
        var keywords: List<String>? = cacheKeywords[packageName]
        if (keywords == null) {
            keywords = loadKeywordsFrom(context, packageName)
            cacheKeywords[packageName] = keywords
        }
        return keywords
    }

    @Suppress("DEPRECATION")
    fun getInstallerOrigin(context: Context): String {
        val origin = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            tryOrNull { context.packageManager.getInstallerPackageName(context.packageName) }
        } else {
            tryOrNull { context.packageManager.getInstallSourceInfo(context.packageName).originatingPackageName }
        }
        return origin ?: "Manual" 
    }

    fun getAppPackageInfo(context: Context): PackageInfo? {
        return tryOrNull {
            context.packageManager.getPackageInfoCompat(context.packageName, 0)
        }
    }

    fun Context.getAppVersionName(): String = getAppPackageInfo(this)?.versionName ?: ""

    @JvmStatic
    fun getSignatures(context: Context?, packageName: String?): AppSignature? =
        getSignatures(context?.packageManager, packageName)

    @JvmStatic
    @SuppressLint("PackageManagerGetSignatures") 
    fun getSignatures(packageManager: PackageManager?, packageName: String?): AppSignature? {
        if (packageManager == null || packageName == null) return null
        val signatures: Array<out Signature> = try {
            val packageInfo = packageManager.getPackageInfoCompat(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            if (packageInfo.signingInfo?.hasMultipleSigners() == true) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                packageInfo.signingInfo?.signingCertificateHistory
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } ?: return AppSignature(packageName)

        val sha512Signatures = mutableSetOf<String>()
        val sha256Signatures = mutableSetOf<String>()
        for (i in signatures.indices) {
            val signature = signatures[i]
            getSha256(signature)?.let {
                sha256Signatures.add(it)
            }
            getSha512(signature)?.let {
                sha512Signatures.add(it)
            }
        }
        return AppSignature(packageName, sha256Signatures.toList(), sha512Signatures.toList())
    }

    private fun getKeywords(packageName: String): List<String> {
        val packageNameParts = packageName.split('.')
        val urlString = packageNameParts.asReversed().joinToString(".")
        val url = urlString.toUrlOrNull()
        return if (url?.isTrademarkDomain == true) {
            packageNameParts
        } else {
            val topPrivateDomain = url?.topPrivateDomain() ?: return packageNameParts
            
            
            
            
            
            
            packageNameParts.drop(topPrivateDomain.count { it == '.' })
        }
    }

    private fun isIgnoredKeyword(value: String): Boolean {
        return (value.length <= 2) || 
            APP_KEYWORD_TO_IGNORE.any { keywordToIgnore ->
                value.trim()
                    .equals(keywordToIgnore, ignoreCase = true) 
            }
    }

    private fun getSha256(signature: Signature): String? {
        return tryOrNull { ByteString.of(*signature.toByteArray()).sha256().hex() }
    }

    private fun getSha512(signature: Signature): String? {
        return tryOrNull { ByteString.of(*signature.toByteArray()).sha512().hex() }
    }

    @VisibleForTesting
    fun loadKeywordsFrom(context: Context, packageName: String): List<String> {
        val keywords = ArrayList<String>()

        
        AutofillExtraDataApplication.getAppForPackage(packageName)?.keywords?.forEach {
            keywords.add(it)
        }

        
        val packageNameParts = getKeywords(packageName).filterNot { keyword ->
            keyword in DOMAINS_EXTENSIONS
        }

        
        keywords.addAll(packageNameParts)

        
        getApplicationNameFromPackage(context, packageName)
            ?.onlyAlphaNumeric()
            ?.split(' ')
            ?.let { keywords.addAll(it) }

        return keywords
            .filterNot { isIgnoredKeyword(it) }
            .map { it.uppercase() }
            .distinct()
    }

    private fun String.onlyAlphaNumeric(): String {
        return replace("[^A-Za-z0-9 ]".toRegex(), "")
    }
}
