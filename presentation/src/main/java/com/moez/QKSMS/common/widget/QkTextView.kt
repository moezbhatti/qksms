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
package com.moez.QKSMS.common.widget

import android.content.Context
import android.util.AttributeSet
import androidx.emoji.widget.EmojiAppCompatTextView
import com.moez.QKSMS.common.util.TextViewStyler
import com.moez.QKSMS.injection.appComponent
import javax.inject.Inject

open class QkTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : EmojiAppCompatTextView(context, attrs) {

    @Inject lateinit var textViewStyler: TextViewStyler

    /**
     * Collapse a multiline list of strings into a single line
     *
     * Ex.
     *
     * Toronto, New York, Los Angeles,
     * Seattle, Portland
     *
     * Will be converted to
     *
     * Toronto, New York, Los Angeles, +2
     */
    var collapseEnabled: Boolean = false

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
            textViewStyler.applyAttributes(this, attrs)
        } else {
            TextViewStyler.applyEditModeAttributes(this, attrs)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (collapseEnabled) {
            layout
                    ?.takeIf { layout -> layout.lineCount > 0 }
                    ?.let { layout -> layout.getEllipsisCount(layout.lineCount - 1) }
                    ?.takeIf { ellipsisCount -> ellipsisCount > 0 }
                    ?.let { ellipsisCount -> text.dropLast(ellipsisCount).lastIndexOf(',') }
                    ?.takeIf { lastComma -> lastComma >= 0 }
                    ?.let { lastComma ->
                        val remainingNames = text.drop(lastComma).count { c -> c == ',' }
                        text = "${text.take(lastComma)}, +$remainingNames"
                    }
        }
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        setLinkTextColor(color)
    }

}