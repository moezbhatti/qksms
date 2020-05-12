package com.moez.QKSMS.feature.compose

import android.os.Build
import android.text.Selection
import android.text.Spannable
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.forEach
import androidx.core.view.isEmpty
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.ClipboardUtils

@RequiresApi(Build.VERSION_CODES.M)
class ActionModeCallback : ActionMode.Callback2() {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        if (menu.isEmpty()) {
            mode.menuInflater.inflate(R.menu.compose_action_menu, menu)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val tag = mode.tag
        if (item.itemId == R.id.copy && tag is TextView) {
            val text = tag.text
            val start = Selection.getSelectionStart(text)
            val end = Selection.getSelectionEnd(text)
            val selection = text.substring(start, end)

            ClipboardUtils.copy(tag.context, selection)
            tag.clearFocus()
            mode.finish()
            return true
        }

        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
    }

}
