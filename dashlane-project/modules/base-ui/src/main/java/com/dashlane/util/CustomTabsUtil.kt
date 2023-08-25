package com.dashlane.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.dashlane.design.theme.color.darkColors
import com.dashlane.design.theme.color.lightColors
import com.dashlane.ui.R

@Suppress("SpreadOperator")
fun TextView.setTextWithLinks(resId: Int, resIdArgToLinks: List<Pair<Int, Uri>>) {
    val params = resIdArgToLinks.map { context.getString(it.first) to it.second }
    val fullText =
        SpannableString(context.getString(resId, *params.map { it.first }.toTypedArray()))
    setTextWithLinks(fullText, params)
}

fun TextView.setTextWithLinks(
    fullText: SpannableString,
    stringArgToLinks: List<Pair<String, Uri>>,
    viewToDisable: View? = null
) {
    stringArgToLinks.forEach { pair ->
        val paramText = pair.first
        val uri = pair.second
        val startIndex = fullText.indexOf(paramText)
        if (startIndex >= 0) {
            val clickSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    viewToDisable?.cancelPendingInputEvents()
                    context.launchUrl(uri)
                }
            }
            fullText.setSpan(
                clickSpan,
                startIndex,
                startIndex + paramText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        movementMethod = LinkMovementMethod.getInstance()
    }
    text = fullText
}

fun Context.launchUrl(url: String) =
    launchUrl(Uri.parse(url))

fun Context.launchUrl(uri: Uri) {
    try {
        CustomTabsIntent.Builder().setShowTitle(true).applyAppTheme().build().launchUrl(this, uri)
    } catch (e: ActivityNotFoundException) {
        val fallback = Intent(Intent.ACTION_VIEW, uri)
        if (fallback.resolveActivity(packageManager) != null) {
            ContextCompat.startActivity(this, fallback, null)
        } else {
            displayNoBrowserWarning(this)
        }
    }
}

fun CustomTabsIntent.Builder.applyAppTheme() = setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
    .setColorSchemeParams(
        CustomTabsIntent.COLOR_SCHEME_DARK,
        CustomTabColorSchemeParams.Builder()
            .setToolbarColor(darkColors.containerAgnosticNeutralStandard.toArgb())
            .setNavigationBarColor(darkColors.textNeutralCatchy.value.toArgb())
            .build()
    )
    .setColorSchemeParams(
        CustomTabsIntent.COLOR_SCHEME_LIGHT,
        CustomTabColorSchemeParams.Builder()
            .setToolbarColor(lightColors.containerAgnosticNeutralStandard.toArgb())
            .setNavigationBarColor(lightColors.textNeutralCatchy.value.toArgb())
            .build()
    )

fun Intent.fallbackCustomTab(packageManager: PackageManager): Intent {
    return if (resolveActivity(packageManager) != null) {
        this
    } else {
        Intent(Intent.ACTION_VIEW, data)
    }
}

fun Context.safelyStartBrowserActivity(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        displayNoBrowserWarning(this)
    }
}

@Suppress("DEPRECATION")
fun FragmentActivity.safelyStartBrowserActivityForResult(intent: Intent, requestCode: Int) {
    try {
        startActivityForResult(intent, requestCode)
    } catch (e: ActivityNotFoundException) {
        displayNoBrowserWarning(this)
    }
}

private fun displayNoBrowserWarning(context: Context) {
    ToasterImpl(context).show(
        text = context.getString(R.string.warning_no_browser_enabled_on_device),
        duration = Toast.LENGTH_LONG
    )
}