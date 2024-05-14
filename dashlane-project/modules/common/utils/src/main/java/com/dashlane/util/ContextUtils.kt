package com.dashlane.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AnimRes
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import java.io.File
import java.io.IOException

inline fun <reified T> Context.startActivity(intentBlock: Intent.() -> Unit = {}) {
    startActivity(Intent(this, T::class.java).apply(intentBlock))
}

inline fun <reified T> Context.startActivity(
    @AnimRes enterResId: Int,
    @AnimRes exitResId: Int,
    intentBlock: Intent.() -> Unit
) {
    val options = ActivityOptions.makeCustomAnimation(this, enterResId, exitResId).toBundle()
    startActivity(Intent(this, T::class.java).apply(intentBlock), options)
}

inline fun <reified T> Context.startService(intentBlock: Intent.() -> Unit): ComponentName? =
    startService(Intent(this, T::class.java).apply(intentBlock))

inline fun <reified T> Activity.startActivityForResult(
    requestCode: Int,
    options: Bundle? = null,
    intentBlock: Intent.() -> Unit = {}
) {
    startActivityForResult(Intent(this, T::class.java).apply(intentBlock), requestCode, options)
}

tailrec fun Context.getBaseActivity(): Activity? {
    if (this is Activity) return this
    return (this as? ContextWrapper)?.baseContext?.takeIf { it != this }?.getBaseActivity()
}

@Throws(IOException::class)
fun Context.readFile(filename: String) =
    File(filesDir, filename).readText()

@Throws(IOException::class)
fun Context.writeFile(filename: String, data: String) =
    File(filesDir, filename).writeText(data)

val Context.telephonyManager
    get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

private val tempArray = IntArray(1)

@ColorInt
fun Context.getThemeAttrColor(@AttrRes attr: Int): Int = getThemeAttr(attr) {
    getColor(0, 0)
}

@Dimension
fun Context.getThemeAttrDimensionPixelSize(@AttrRes attr: Int): Int = getThemeAttr(attr) {
    getDimensionPixelSize(0, 0)
}

@AnyRes
fun Context.getThemeAttrResourceId(@AttrRes attr: Int): Int = getThemeAttr(attr) {
    getResourceId(0, 0)
}

fun Context.getThemeAttrDrawable(@AttrRes attr: Int): Drawable = getThemeAttr(attr) {
    getDrawable(0)!!
}

@AttrRes
fun Context.getAttr(@AttrRes attr: Int, @AttrRes fallbackAttr: Int): Int = getThemeAttr(attr) {
    val defValue = 0
    return getResourceId(0, defValue).takeIf { it != defValue }
        ?.let { attr } ?: fallbackAttr
}

private inline fun <T> Context.getThemeAttr(@AttrRes attr: Int, block: TypedArray.() -> T): T {
    tempArray[0] = attr
    val a = obtainStyledAttributes(null, tempArray)
    try {
        return a.block()
    } finally {
        a.recycle()
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerExportedReceiverCompat(broadcastReceiver: BroadcastReceiver, intentFilter: IntentFilter) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
    } else {
        registerReceiver(broadcastReceiver, intentFilter)
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerExportedReceiverCompat(
    broadcastReceiver: BroadcastReceiver,
    intentFilter: IntentFilter,
    s: String,
    handler: Handler?
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(broadcastReceiver, intentFilter, s, handler, Context.RECEIVER_EXPORTED)
    } else {
        registerReceiver(broadcastReceiver, intentFilter, s, handler)
    }
}