package com.dashlane.listeners.edittext

import android.text.Editable
import android.text.TextWatcher
import com.dashlane.lock.LockManager

class NoLockEditTextWatcher(private val lockManager: LockManager) : TextWatcher {
    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
    override fun afterTextChanged(editable: Editable) {
        lockManager.setLastActionTimestampToNow()
    }
}