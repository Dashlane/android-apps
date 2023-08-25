package com.dashlane.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;

public class ThreadHelper {

    private Handler mHandler;

    public ThreadHelper() {
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    @MainThread
    public void init() {
        if (!isMainThread()) {
            throw new RuntimeException("ThreadHelper.init() must be call on the MainThread");
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void runOnBackgroundThread(final Runnable runnable) {
        new Thread(runnable).start();
    }

    public void runOnMainThread(Runnable runnable) {
        post(runnable);
    }

    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void postDelayed(Runnable runnable, long timeMs) {
        mHandler.postDelayed(runnable, timeMs);
    }

    public void removeCallbacks(Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }

    public boolean isMainLooper() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
