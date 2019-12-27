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

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.model.Conversation
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
    private val colors: Colors,
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator
) : QkRealmAdapter<Conversation>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.conversation_list_item, parent, false)

        if (viewType == 1) {
            val textColorPrimary = parent.context.resolveThemeColor(android.R.attr.textColorPrimary)

            view.title.setTypeface(view.title.typeface, Typeface.BOLD)

            view.snippet.setTypeface(view.snippet.typeface, Typeface.BOLD)
            view.snippet.setTextColor(textColorPrimary)
            view.snippet.maxLines = 5

            view.unread.isVisible = true
            view.unread.setTint(colors.theme().theme)

            view.date.setTypeface(view.date.typeface, Typeface.BOLD)
            view.date.setTextColor(textColorPrimary)
        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener
                when (toggleSelection(conversation.id, false)) {
                    true -> {
                        view.isActivated = isSelected(conversation.id)
                        // add selection image
                        flipAvatarPic(view)
                    }
                    false -> navigator.showConversation(conversation.id)
                }
            }
            view.setOnLongClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnLongClickListener true
                toggleSelection(conversation.id)
                view.isActivated = isSelected(conversation.id)
                // add selection image
                flipAvatarPic(view)
                true
            }
        }
    }

    override fun onBindViewHolder(viewHolder: QkViewHolder, position: Int) {
        val conversation = getItem(position) ?: return
        val view = viewHolder.containerView

        val origActivated = view.isActivated
        view.isActivated = isSelected(conversation.id)
        // add selection image
        if (origActivated != view.isActivated) flipAvatarPic(view)

        view.avatars.contacts = conversation.recipients
        view.title.collapseEnabled = conversation.recipients.size > 1
        view.title.text = conversation.getTitle()
        view.date.text = dateFormatter.getConversationTimestamp(conversation.date)
        view.snippet.text = when {
            conversation.draft.isNotEmpty() -> context.getString(R.string.main_draft, conversation.draft)
            conversation.me -> context.getString(R.string.main_sender_you, conversation.snippet)
            else -> conversation.snippet
        }
        view.pinned.isVisible = conversation.pinned
    }

    override fun getItemId(index: Int): Long {
        return getItem(index)!!.id
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.unread == false) 0 else 1
    }

    // add selection image
    private fun flipAvatarPic(parentView: View) {
        if (parentView.isActivated) {
            animatorFlipViews(parentView.avatars, parentView.checked)
        } else {
            animatorFlipViews(parentView.checked, parentView.avatars)
        }
    }

    private fun animatorFlipViews(oldView: View, newView: View) {
        val animatorSetOldView = AnimatorInflater.loadAnimator(oldView.context, R.animator.flip_front) as AnimatorSet
        animatorSetOldView.setTarget(oldView)
        val animatorSetNewView = AnimatorInflater.loadAnimator(newView.context, R.animator.flip_back) as AnimatorSet
        animatorSetNewView.setTarget(newView)

        val combAnimatorSet = AnimatorSet()
        combAnimatorSet.playSequentially(animatorSetOldView,animatorSetNewView)
        combAnimatorSet.start()
    }
}