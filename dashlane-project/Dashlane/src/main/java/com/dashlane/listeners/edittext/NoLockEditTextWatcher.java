package com.dashlane.listeners.edittext;

import android.text.Editable;
import android.text.TextWatcher;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.session.Session;


public class NoLockEditTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) return;
        SingletonProvider.getComponent().getLockRepository()
                         .getLockManager(session).setLastActionTimestampToNow();
    }
}
