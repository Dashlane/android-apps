package com.dashlane.util

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

fun Context.dpToPx(dp: Float): Float = resources.dpToPx(dp)

fun Context.dpToPx(dp: Int): Int = resources.dpToPx(dp.toFloat()).toInt()

fun Resources.dpToPx(dp: Float): Float = displayMetrics.dpToPx(dp)

fun DisplayMetrics.dpToPx(dp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this)

fun Context.spToPx(sp: Float): Float = resources.spToPx(sp)

fun Resources.spToPx(sp: Float): Float = displayMetrics.spToPx(sp)

fun DisplayMetrics.spToPx(sp: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, this)
