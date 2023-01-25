package com.dashlane.authenticator

import android.content.Context
import com.dashlane.util.PackageUtilities

const val PACKAGE_NAME_AUTHENTICATOR_APP = "com.dashlane.authenticator"

fun Context.isAuthenticatorAppInstalled() =
    PackageUtilities.getPackageInfoFromPackage(this, PACKAGE_NAME_AUTHENTICATOR_APP) != null