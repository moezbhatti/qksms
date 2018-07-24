/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.feature.compose

import android.app.Activity
import android.graphics.Rect
import android.os.Build
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
import androidx.annotation.RequiresApi

class ComposeWindowCallback(private val localCallback: Window.Callback, private val activity: Activity) : Window.Callback {

    override fun dispatchKeyEvent(keyEvent: KeyEvent): Boolean {
        return localCallback.dispatchKeyEvent(keyEvent)
    }

    override fun dispatchKeyShortcutEvent(keyEvent: KeyEvent): Boolean {
        return localCallback.dispatchKeyShortcutEvent(keyEvent)
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            val v = activity.currentFocus
            when (v) {
                is DetailedChipView -> {
                    val rect = Rect().apply { v.getGlobalVisibleRect(this) }
                    if (!rect.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())) {
                        v.hide()
                        return true
                    }
                }
            }
        }
        return localCallback.dispatchTouchEvent(motionEvent)
    }

    override fun dispatchTrackballEvent(motionEvent: MotionEvent): Boolean {
        return localCallback.dispatchTrackballEvent(motionEvent)
    }

    override fun dispatchGenericMotionEvent(motionEvent: MotionEvent): Boolean {
        return localCallback.dispatchGenericMotionEvent(motionEvent)
    }

    override fun dispatchPopulateAccessibilityEvent(accessibilityEvent: AccessibilityEvent): Boolean {
        return localCallback.dispatchPopulateAccessibilityEvent(accessibilityEvent)
    }

    override fun onCreatePanelView(i: Int): View? {
        return localCallback.onCreatePanelView(i)
    }

    override fun onCreatePanelMenu(i: Int, menu: Menu): Boolean {
        return localCallback.onCreatePanelMenu(i, menu)
    }

    override fun onPreparePanel(i: Int, view: View, menu: Menu): Boolean {
        return localCallback.onPreparePanel(i, view, menu)
    }

    override fun onMenuOpened(i: Int, menu: Menu): Boolean {
        return localCallback.onMenuOpened(i, menu)
    }

    override fun onMenuItemSelected(i: Int, menuItem: MenuItem): Boolean {
        return localCallback.onMenuItemSelected(i, menuItem)
    }

    override fun onWindowAttributesChanged(layoutParams: WindowManager.LayoutParams) {
        localCallback.onWindowAttributesChanged(layoutParams)
    }

    override fun onContentChanged() {
        localCallback.onContentChanged()
    }

    override fun onWindowFocusChanged(b: Boolean) {
        localCallback.onWindowFocusChanged(b)
    }

    override fun onAttachedToWindow() {
        localCallback.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        localCallback.onDetachedFromWindow()
    }

    override fun onPanelClosed(i: Int, menu: Menu) {
        localCallback.onPanelClosed(i, menu)
    }

    override fun onSearchRequested(): Boolean {
        return localCallback.onSearchRequested()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onSearchRequested(searchEvent: SearchEvent): Boolean {
        return localCallback.onSearchRequested(searchEvent)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onWindowStartingActionMode(callback: ActionMode.Callback, i: Int): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback, i)
    }

    override fun onActionModeStarted(actionMode: ActionMode) {
        localCallback.onActionModeStarted(actionMode)
    }

    override fun onActionModeFinished(actionMode: ActionMode) {
        localCallback.onActionModeFinished(actionMode)
    }
}
