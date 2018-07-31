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
package com.moez.QKSMS.feature.conversations

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject


class ConversationItemTouchCallback @Inject constructor(
        colors: Colors,
        lifecycle: Lifecycle,
        prefs: Preferences,
        private val context: Context
) : ItemTouchHelper.SimpleCallback(0, 0) {

    val swipes: Subject<Pair<Long, Int>> = PublishSubject.create()

    /**
     * Setting the adapter allows us to animate back to the original position
     */
    var adapter: RecyclerView.Adapter<*>? = null

    private val paint = Paint()
    private var rightAction = 0
    private var rightIcon: Bitmap? = null
    private var leftAction = 0
    private var leftIcon: Bitmap? = null

    private val iconLength = 24.dpToPx(context)

    init {
        colors.themeObservable()
                .autoDisposable(lifecycle.scope())
                .subscribe { theme -> paint.color = theme.theme }

        Observables
                .combineLatest(prefs.swipeRight.asObservable(), prefs.swipeLeft.asObservable(), colors.themeObservable()) { right, left, theme ->
                    rightAction = right
                    rightIcon = iconForAction(right, theme.textPrimary)
                    leftAction = left
                    leftIcon = iconForAction(left, theme.textPrimary)
                    setDefaultSwipeDirs((if (right == Preferences.SWIPE_ACTION_NONE) 0 else ItemTouchHelper.RIGHT)
                            or (if (left == Preferences.SWIPE_ACTION_NONE) 0 else ItemTouchHelper.LEFT))
                }
                .autoDisposable(lifecycle.scope())
                .subscribe()
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            if (dX > 0) {
                c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), paint)
                c.drawBitmap(rightIcon, itemView.left.toFloat() + iconLength,
                        itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - (rightIcon?.height
                                ?: 0)) / 2, paint)
            } else if (dX < 0) {
                c.drawRect(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)
                c.drawBitmap(leftIcon, itemView.right.toFloat() - iconLength - (leftIcon?.width ?: 0),
                        itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - (leftIcon?.height
                                ?: 0)) / 2, paint)
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipes.onNext(Pair(viewHolder.itemId, direction))

        // This will trigger the animation back to neutral state
        val action = if (direction == ItemTouchHelper.RIGHT) rightAction else leftAction
        if (action != Preferences.SWIPE_ACTION_ARCHIVE && action != Preferences.SWIPE_ACTION_READ) {
            adapter?.notifyItemChanged(viewHolder.adapterPosition)
        }
    }

    private fun iconForAction(action: Int, tint: Int): Bitmap? {
        val res = when (action) {
            Preferences.SWIPE_ACTION_ARCHIVE -> R.drawable.ic_archive_black_24dp
            Preferences.SWIPE_ACTION_DELETE -> R.drawable.ic_delete_white_24dp
            Preferences.SWIPE_ACTION_CALL -> R.drawable.ic_call_white_24dp
            Preferences.SWIPE_ACTION_READ -> R.drawable.ic_check_white_24dp
            else -> null
        }

        return res?.let(context.resources::getDrawable)
                ?.apply { setTint(tint) }
                ?.toBitmap(iconLength, iconLength)
    }

}