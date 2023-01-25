package com.dashlane.util

import android.annotation.SuppressLint
import android.app.backup.BackupManager
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.ui.R
import com.dashlane.util.Toaster.Duration
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@SuppressLint("ToastUsage")
class ToasterImpl @Inject constructor(@ApplicationContext private val context: Context) : Toaster {

    private val darkThemeHelper: DarkThemeHelper by lazy {
        DarkThemeHelper(GlobalPreferencesManager(context, BackupManager(context)))
    }

    override fun show(@StringRes resId: Int, @Duration duration: Int) =
        show(context.resources?.getText(resId), duration, Toaster.Position.BOTTOM)

    override fun show(@StringRes resId: Int, @Duration duration: Int, position: Toaster.Position) =
        show(context.resources?.getText(resId), duration, position)

    override fun show(text: CharSequence?, @Duration duration: Int) = show(text, duration, Toaster.Position.BOTTOM)

    override fun show(text: CharSequence?, @Duration duration: Int, position: Toaster.Position) {
        makeToast(text, duration, position).show()
    }

    private fun makeToast(text: CharSequence?, @Duration duration: Int, position: Toaster.Position): Toast {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            makeDefaultToast(text, duration, position)
        } else {
            makeCustomToast(text, duration, position)
        }
    }

    private fun makeDefaultToast(text: CharSequence?, @Duration duration: Int, position: Toaster.Position): Toast {
        return Toast.makeText(context, text, duration).apply {
            setGravity(
                Gravity.FILL_HORIZONTAL or position.gravity,
                0,
                0
            )
        }
    }

    private fun makeCustomToast(text: CharSequence?, @Duration duration: Int, position: Toaster.Position): Toast {
        val toast = Toast(context)
        prepareToast(text, toast, duration, position)
        return toast
    }

    @Suppress("DEPRECATION")
    private fun prepareToast(
        text: CharSequence?,
        toast: Toast,
        duration: Int,
        position: Toaster.Position
    ) {
        val toastLayout = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        val textView: TextView = toastLayout.findViewById(R.id.text)

        val (textColor, backgroundTint) = getThemedColors()

        textView.apply {
            this.text = text
            setTextColor(textColor)
            background.setTint(backgroundTint)
        }
        
        toast.apply {
            setGravity(
                Gravity.FILL_HORIZONTAL or position.gravity,
                0,
                0
            )
            this.duration = duration
            view = toastLayout
        }
    }

    private fun getThemedColors(): Pair<Int, Int> {
        return if (darkThemeHelper.isDarkTheme(context)) {
            context.getColor(R.color.toast_font_color_dark) to
                    context.getColor(R.color.toast_background_color_dark)
        } else {
            context.getColor(R.color.toast_font_color) to
                    context.getColor(R.color.toast_background_color)
        }
    }
}

fun Context.showToaster(
    text: CharSequence?,
    @Duration duration: Int,
    position: Toaster.Position = Toaster.Position.BOTTOM
) {
    ToasterImpl(this).show(text, duration, position)
}

fun Context.showToaster(
    @StringRes text: Int,
    @Duration duration: Int,
    position: Toaster.Position = Toaster.Position.BOTTOM
) {
    ToasterImpl(this).show(text, duration, position)
}