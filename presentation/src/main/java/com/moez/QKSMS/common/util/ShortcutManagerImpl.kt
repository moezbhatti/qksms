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
package com.moez.QKSMS.common.util

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.telephony.PhoneNumberUtils
import com.moez.QKSMS.R
import com.moez.QKSMS.feature.compose.ComposeActivity
import com.moez.QKSMS.model.Conversation
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import com.moez.QKSMS.util.GlideApp
import com.moez.QKSMS.util.tryOrNull
import me.leolin.shortcutbadger.ShortcutBadger
import javax.inject.Inject

class ShortcutManagerImpl @Inject constructor(
        private val context: Context,
        private val conversationRepo: ConversationRepository,
        private val messageRepo: MessageRepository
) : com.moez.QKSMS.manager.ShortcutManager {

    override fun updateBadge() {
        val count = messageRepo.getUnreadCount().toInt()
        ShortcutBadger.applyCount(context, count)
    }

    override fun updateShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            if (shortcutManager.isRateLimitingActive) return

            shortcutManager.dynamicShortcuts = conversationRepo.getTopConversations()
                    .take(shortcutManager.maxShortcutCountPerActivity - shortcutManager.manifestShortcuts.size)
                    .map { conversation -> createShortcutForConversation(conversation, shortcutManager) }
        }
    }

    @TargetApi(25)
    private fun createShortcutForConversation(conversation: Conversation, shortcutManager: ShortcutManager): ShortcutInfo {
        val icon = when {
            conversation.recipients.size == 1 -> {
                val address = conversation.recipients.first()!!.address
                val request = GlideApp.with(context)
                        .asBitmap()
                        .circleCrop()
                        .load(PhoneNumberUtils.stripSeparators(address))
                        .submit(shortcutManager.iconMaxWidth, shortcutManager.iconMaxHeight)
                val bitmap = tryOrNull(false) { request.get() }

                if (bitmap != null) Icon.createWithBitmap(bitmap)
                else Icon.createWithResource(context, R.mipmap.ic_shortcut_person)
            }

            else -> Icon.createWithResource(context, R.mipmap.ic_shortcut_people)
        }

        val intent = Intent(context, ComposeActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .putExtra("threadId", conversation.id)

        return ShortcutInfo.Builder(context, "${conversation.id}")
                .setShortLabel(conversation.getTitle())
                .setLongLabel(conversation.getTitle())
                .setIcon(icon)
                .setIntent(intent)
                .build()
    }

}