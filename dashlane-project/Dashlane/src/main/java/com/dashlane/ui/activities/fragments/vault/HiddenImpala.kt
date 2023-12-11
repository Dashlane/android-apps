package com.dashlane.ui.activities.fragments.vault

import android.app.Activity
import android.view.View
import com.dashlane.R
import com.dashlane.ui.activities.fragments.vault.HiddenImpala.ClickType.LONG
import com.dashlane.ui.activities.fragments.vault.HiddenImpala.ClickType.SHORT
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.SnackbarUtils
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class HiddenImpala(private val activity: Activity) {

    enum class ClickType {
        SHORT,
        LONG
    }

    data class MorseLetter(val letter: Char, val morseCode: List<ClickType>)

    private val code = listOf(
        MorseLetter('D', listOf(LONG, SHORT, SHORT)),
        MorseLetter('A', listOf(SHORT, LONG)),
        MorseLetter('S', listOf(SHORT, SHORT, SHORT)),
        MorseLetter('H', listOf(SHORT, SHORT, SHORT, SHORT))
    )
    private var currentLetterIndex = 0
    private var currentClickIndex = 0

    fun onLongClick() {
        validateCode(LONG)
    }

    fun onClick() {
        validateCode(SHORT)
    }

    private fun validateCode(clickType: ClickType) {
        val currentLetter = code[currentLetterIndex]
        if (currentLetter.morseCode[currentClickIndex] != clickType) {
            
            reset()
            return
        }
        
        currentClickIndex++
        if (currentClickIndex < currentLetter.morseCode.size) return
        
        currentClickIndex = 0
        currentLetterIndex++
        val snackbar = SnackbarUtils.showSnackbar(
            activity,
            code.joinToString(separator = " ", limit = currentLetterIndex, transform = { it.letter.toString() }),
            Snackbar.LENGTH_SHORT
        )
        if (currentLetterIndex < code.size) return
        
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                unlockImpala()
                snackbar.removeCallback(this)
            }
        })
        reset()
    }

    private fun reset() {
        currentLetterIndex = 0
        currentClickIndex = 0
    }

    private fun unlockImpala() {
        DialogHelper()
            .builder(activity)
            .setTitle(R.string.hidden_impala_title)
            .setView(R.layout.hidden_impala)
            .setPositiveButton(R.string.hidden_impala_positive_button, null)
            .show()
        reset()
    }

    companion object {
        fun configureForHomeActivity(activity: Activity) {
            val toolbar = activity.findViewById<View>(R.id.toolbar)
            val hiddenImpala = HiddenImpala(activity)
            toolbar.setOnClickListener { hiddenImpala.onClick() }
            toolbar.setOnLongClickListener {
                hiddenImpala.onLongClick()
                true
            }
        }
    }
}