package com.dashlane.ext.application

import com.dashlane.core.helpers.AppSignature
import com.dashlane.url.toUrlDomain
import com.dashlane.url.toUrlDomainOrNull

object AutofillExtraDataApplication {
    private val autofillExtraData = listOf(
        app(
            "com.google.android.gm",
            "google.com",
            "f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83",
            setOf("gmail"),
            setOf("gmail.com")
        ),
        app(
            "com.facebook.katana",
            "facebook.com",
            "e3f9e1e0cf99d0e56a055ba65e241b3399f7cea524326b0cdd6ec1327ed0fdc1",
            null,
            setOf("instagram.com")
        ),
        app(
            "com.ebay.mobile",
            "ebay.com",
            "85b4015497154dfa89de308771a9fc61a64b19af1a3fb03256e8cc3a795567e5",
            null,
            setOf("paypal.com")
        ),
        app(
            "de.number26.android",
            "n26.com",
            "800f89b010f9ef7cb4884ed6edcfd0bae50e7f484ec07517fdf661d69d650add",
            setOf("n26"),
            null
        ),
        app(
            "com.instagram.android",
            "instagram.com",
            "5f3e50f435583c9ae626302a71f7340044087a7e2c60adacfc254205a993e305",
            null,
            setOf("facebook.com")
        ),
        app(
            "com.microsoft.skydrive",
            "live.com",
            "a0794215278a567e887af6cde015a5e88414ef640f7dab3855a3e779658be778",
            null,
            setOf("skydrive.com")
        ),
        app(
            "com.snapchat.android",
            "snapchat.com",
            "2f1caafca1ed30d0b4e38863eefabea0e815711fa4cf79b822519a8259d95a58",
            setOf("snapchat"),
            null
        ),
        app(
            "com.arlo.app",
            "arlo.com",
            "81ee3b93b3df40bb3e06ae30479d00ac1be87685b4967d862222684ace96949f",
            null,
            setOf("netgear.com")
        ),
        app(
            "com.disney.disneyplus",
            "disneyplus.com",
            "b4d251e979ea974d8f070feb5ab69fc4c2873f4e8add421ff63ad5f2789d63d4",
            setOf("disney+"),
            null
        ),
        app(
            "com.google.android.gms",
            "google.com",
            "f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83",
            null,
            setOf("gmail.com")
        )
    )

    private fun app(
        packageName: String,
        domain: String,
        signature: String,
        keywords: Set<String>?,
        allowedDomains: Set<String>?
    ) = KnownApplication.App(
        packageName = packageName,
        signatures = AppSignature(packageName, listOf(signature)),
        mainUrlDomain = domain.toUrlDomain(),
        allowedDomains = allowedDomains?.mapNotNull { it.toUrlDomainOrNull() }?.toSet(),
        keywords = keywords
    )

    fun getAppForPackage(packageName: String) = autofillExtraData.singleOrNull { it.packageName == packageName }
}