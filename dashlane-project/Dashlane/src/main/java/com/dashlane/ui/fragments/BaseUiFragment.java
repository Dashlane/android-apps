package com.dashlane.ui.fragments;

import android.os.Bundle;
import android.view.View;

import com.dashlane.crashreport.CrashReporter;
import com.dashlane.dagger.singleton.SingletonProvider;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



public class BaseUiFragment extends Fragment {

    private CrashReporter mCrashReporter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCrashReporter = SingletonProvider.getComponent().getCrashReporter();
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.CREATED);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.VIEW_CREATED);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.STARTED);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.RESUMED);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.PAUSED);
    }

    @Override
    public void onStop() {
        super.onStop();
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.STOPPED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.DESTROYED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCrashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.VIEW_DESTROYED);
    }
}
