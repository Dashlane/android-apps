package com.dashlane.createaccount

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.dashlane.util.getRelativePosition
import com.dashlane.util.getThemeAttrColor

class LoginEmailAnimator(context: Context) {

    val alpha = Color.alpha(context.getThemeAttrColor(android.R.attr.textColorSecondary)) / 255.0f

    fun animate(dest: TextView, origin: EditText) {
        dest.visibility = View.INVISIBLE

        
        val x = dest.x
        val y = dest.y

        
        val text = origin.text.toString()
        dest.text = text
        dest.alpha = 1.0f
        dest.x = origin.getRelativePosition(dest.parent, View::getX)
        dest.y = origin.getRelativePosition(dest.parent, View::getY)

        
        dest.visibility = View.VISIBLE
        origin.text.clear()

        
        dest.animate().x(x).y(y).alpha(alpha).withEndAction { origin.setText(text) }
    }
}