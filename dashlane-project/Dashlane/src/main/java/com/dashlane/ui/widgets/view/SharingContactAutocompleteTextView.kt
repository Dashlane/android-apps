package com.dashlane.ui.widgets.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.R
import com.dashlane.ui.screens.sharing.SharingContact
import com.dashlane.ui.screens.sharing.SharingContact.SharingContactUser
import com.dashlane.ui.widgets.view.chips.SharingContactChipsView
import com.dashlane.util.isValidEmail
import com.tokenautocomplete.TokenCompleteTextView

class SharingContactAutocompleteTextView(context: Context, attrs: AttributeSet) :
    TokenCompleteTextView<SharingContact>(context, attrs) {

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        performBestGuess(false)
    }

    override fun getViewForObject(obj: SharingContact): View {
        val view = layoutInflater.inflate(
            R.layout.chips_text_sharing_contact,
            this@SharingContactAutocompleteTextView.parent as ViewGroup,
            false
        ) as SharingContactChipsView
        view.setName(obj.toString())
        return view
    }

    override fun defaultObject(completionText: String): SharingContact? {
        return if (completionText.isValidEmail()) {
            SharingContactUser(completionText)
        } else {
            null
        }
    }
}
