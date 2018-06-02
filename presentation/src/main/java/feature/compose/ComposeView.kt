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
package feature.compose

import android.net.Uri
import android.support.v13.view.inputmethod.InputContentInfoCompat
import common.base.QkView
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import model.Attachment
import model.Contact
import model.Message

interface ComposeView : QkView<ComposeState> {

    val activityVisibleIntent: Observable<Boolean>
    val queryChangedIntent: Observable<CharSequence>
    val queryBackspaceIntent: Observable<*>
    val queryEditorActionIntent: Observable<Int>
    val chipSelectedIntent: Subject<Contact>
    val chipDeletedIntent: Subject<Contact>
    val menuReadyIntent: Observable<Unit>
    val optionsItemIntent: Observable<Int>
    val messageClickIntent: Subject<Message>
    val messagesSelectedIntent: Observable<List<Long>>
    val cancelSendingIntent: Subject<Message>
    val attachmentDeletedIntent: Subject<Attachment>
    val textChangedIntent: Observable<CharSequence>
    val attachIntent: Observable<Unit>
    val cameraIntent: Observable<*>
    val galleryIntent: Observable<*>
    val attachmentSelectedIntent: Observable<Uri>
    val inputContentIntent: Observable<InputContentInfoCompat>
    val changeSimIntent: Observable<*>
    val sendIntent: Observable<Unit>

    fun clearSelection()
    fun requestStoragePermission()
    fun requestCamera()
    fun requestGallery()
    fun setDraft(draft: String)
    fun scrollToMessage(id: Long)
    val backPressedIntent: Observable<Unit>

}