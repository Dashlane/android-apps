package com.dashlane.login.pages

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import com.dashlane.R
import com.dashlane.lock.UnlockEvent
import com.dashlane.ui.adapter.SpinnerAdapterDefaultValueString

object LoginSwitchAccountUtil {
    fun setupSpinner(
        spinner: Spinner,
        email: String?,
        loginHistory: List<String>,
        onClickChangeAccount: (String?) -> Unit
    ) {
        spinner.run {
            val items =
                listOf(email) + (loginHistory - email) + context.getString(R.string.login_change_account)
            adapter = object : SpinnerAdapterDefaultValueString(
                context,
                R.layout.spinner_item_dropdown,
                R.layout.spinner_item_preview,
                items,
                email
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    return (super.getView(position, convertView, parent) as TextView).apply {
                        
                        setTextColor(Color.TRANSPARENT)
                    }
                }
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, selectedIndex: Int, p3: Long) {
                    when (selectedIndex) {
                        in 1..items.size - 2 -> onClickChangeAccount(items[selectedIndex])
                        items.size - 1 -> onClickChangeAccount(null)
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) = Unit
            }
        }
    }

    fun canSwitch(reason: UnlockEvent.Reason?) = when (reason) {
        null, is UnlockEvent.Reason.AppAccess, is UnlockEvent.Reason.AccessFromExternalComponent -> true
        else -> false
    }
}