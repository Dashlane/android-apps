package com.dashlane.util

import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SearchEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent



abstract class DelegateWindowCallback(private val originalCallback: Window.Callback?) : Window.Callback {

    override fun onActionModeFinished(mode: ActionMode?) {
        originalCallback?.onActionModeFinished(mode)
    }

    override fun onCreatePanelView(featureId: Int): View? {
        return originalCallback?.onCreatePanelView(featureId)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return originalCallback?.dispatchTouchEvent(event) ?: return false
    }

    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
        return originalCallback?.onCreatePanelMenu(featureId, menu) ?: return false
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback?): ActionMode? {
        return originalCallback?.onWindowStartingActionMode(callback)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        return originalCallback?.onWindowStartingActionMode(callback, type)
    }

    override fun onAttachedToWindow() {
        originalCallback?.onAttachedToWindow()
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        return originalCallback?.dispatchGenericMotionEvent(event) ?: return false
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        return originalCallback?.dispatchPopulateAccessibilityEvent(event) ?: return false
    }

    override fun dispatchTrackballEvent(event: MotionEvent?): Boolean {
        return originalCallback?.dispatchTrackballEvent(event) ?: return false
    }

    override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean {
        return originalCallback?.dispatchKeyShortcutEvent(event) ?: return false
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return originalCallback?.dispatchKeyEvent(event) ?: return false
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        return originalCallback?.onMenuOpened(featureId, menu) ?: return false
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        originalCallback?.onPanelClosed(featureId, menu)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        return originalCallback?.onMenuItemSelected(featureId, item) ?: return false
    }

    override fun onDetachedFromWindow() {
        originalCallback?.onDetachedFromWindow()
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        return originalCallback?.onPreparePanel(featureId, view, menu) ?: return false
    }

    override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams?) {
        originalCallback?.onWindowAttributesChanged(attrs)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        originalCallback?.onWindowFocusChanged(hasFocus)
    }

    override fun onContentChanged() {
        originalCallback?.onContentChanged()
    }

    override fun onSearchRequested(): Boolean {
        return originalCallback?.onSearchRequested() ?: return false
    }

    override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
        return originalCallback?.onSearchRequested(searchEvent) ?: return false
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        originalCallback?.onActionModeStarted(mode)
    }
}