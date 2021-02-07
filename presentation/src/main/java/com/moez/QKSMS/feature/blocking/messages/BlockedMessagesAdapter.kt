/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
package com.moez.QKSMS.feature.blocking.messages

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkRealmAdapter
import com.moez.QKSMS.common.base.QkViewHolder
import com.moez.QKSMS.common.util.DateFormatter
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.databinding.BlockedListItemBinding
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.util.Preferences
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class BlockedMessagesAdapter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter
) : QkRealmAdapter<Conversation, BlockedListItemBinding>() {

    val clicks: PublishSubject<Long> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder<BlockedListItemBinding> {
        return QkViewHolder(parent, BlockedListItemBinding::inflate).apply {
            if (viewType == 0) {
                binding.title.setTypeface(binding.title.typeface, Typeface.BOLD)
                binding.date.setTypeface(binding.date.typeface, Typeface.BOLD)
                binding.date.setTextColor(parent.context.resolveThemeColor(android.R.attr.textColorPrimary))
            }

            binding.root.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener
                when (toggleSelection(conversation.id, false)) {
                    true -> binding.root.isActivated = isSelected(conversation.id)
                    false -> clicks.onNext(conversation.id)
                }
            }

            binding.root.setOnLongClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnLongClickListener true
                toggleSelection(conversation.id)
                binding.root.isActivated = isSelected(conversation.id)
                true
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder<BlockedListItemBinding>, position: Int) {
        val conversation = getItem(position) ?: return

        holder.binding.root.isActivated = isSelected(conversation.id)

        holder.binding.avatars.recipients = conversation.recipients
        holder.binding.title.collapseEnabled = conversation.recipients.size > 1
        holder.binding.title.text = conversation.getTitle()
        holder.binding.date.text = dateFormatter.getConversationTimestamp(conversation.date)

        holder.binding.blocker.text = when (conversation.blockingClient) {
            Preferences.BLOCKING_MANAGER_CC -> context.getString(R.string.blocking_manager_call_control_title)
            Preferences.BLOCKING_MANAGER_SIA -> context.getString(R.string.blocking_manager_sia_title)
            else -> null
        }

        holder.binding.reason.text = conversation.blockReason
        holder.binding.blocker.isVisible = holder.binding.blocker.text.isNotEmpty()
        holder.binding.reason.isVisible = holder.binding.blocker.text.isNotEmpty()
    }

    override fun getItemViewType(position: Int): Int {
        val conversation = getItem(position)
        return if (conversation?.unread == false) 1 else 0
    }

}
