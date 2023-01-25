package com.dashlane.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.Size
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

object DeviceUtils {

    @JvmStatic
    fun hideKeyboard(v: View?) {
        v?.let {
            val mgr = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            mgr.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity?) {
        activity?.hideSoftKeyboard()
    }

    fun showKeyboard(v: View) {
        val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }

    @JvmStatic
    fun getDeviceCountry(context: Context): String {
        var country = "us"
        try {
            val deviceCountry = context.telephonyManager.deviceCountry
            if (deviceCountry != null) {
                return deviceCountry
            }
        } catch (e: Exception) {
            
        }

        try {
            country = Locale.getDefault().country.lowercase(Locale.US)
        } catch (e: Exception) {
            
        }

        return country
    }

    @JvmStatic
    fun getNavigationDrawerWidth(context: Context): Int {
        val screenWidth = getScreenSize(context)[0]
        val screenWidthLessDefaultMargin = screenWidth - getDips(context, 56)
        return min(
            screenWidthLessDefaultMargin, getDips(context, 320)
        )
    }

    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    @JvmStatic
    fun getScreenSize(context: Context): IntArray {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val size = getSize(wm)
            intArrayOf(size.width, size.height)
        } else {
            getSizeLegacy(wm)
        }
    }

    private fun getDips(context: Context, padding: Int): Int {
        return context.dpToPx(padding.toFloat()).roundToInt()
    }

    @TargetApi(Build.VERSION_CODES.R)
    private fun getSize(windowManager: WindowManager): Size {
        val metrics = windowManager.currentWindowMetrics
        
        val windowInsets = metrics.windowInsets
        val insets = windowInsets.getInsets(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        
        return Size(metrics.bounds.width() - insetsWidth, metrics.bounds.height() - insetsHeight)
    }

    @Suppress("DEPRECATION")
    private fun getSizeLegacy(windowManager: WindowManager): IntArray {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        return intArrayOf(width, height)
    }
}