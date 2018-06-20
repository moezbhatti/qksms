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
package com.moez.QKSMS.feature.conversationinfo

import com.moez.QKSMS.common.base.QkView
import io.reactivex.Observable

interface ConversationInfoView : QkView<ConversationInfoState> {

    val nameIntent: Observable<*>
    val nameChangedIntent: Observable<String>
    val notificationsIntent: Observable<Unit>
    val themeIntent: Observable<Unit>
    val archiveIntent: Observable<Unit>
    val blockIntent: Observable<Unit>
    val deleteIntent: Observable<Unit>
    val confirmDeleteIntent: Observable<Unit>

    fun showNameDialog(name: String)
    fun showDeleteDialog()

}