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
package com.moez.QKSMS.feature.gallery

import android.content.Context
import android.content.Intent
import com.moez.QKSMS.R
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.common.util.extensions.makeToast
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.interactor.SaveImage
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.repository.MessageRepository
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject

class GalleryViewModel @Inject constructor(
        private val intent: Intent,
        private val context: Context,
        private val conversationRepo: ConversationRepository,
        private val messageRepo: MessageRepository,
        private val saveImage: SaveImage
) : QkViewModel<GalleryView, GalleryState>(GalleryState()) {

    private val partIdFlowable = Flowable.just(intent)
            .map { it.getLongExtra("partId", 0L) }
            .filter { partId -> partId != 0L }

    init {
        disposables += partIdFlowable
                .mapNotNull { partId -> messageRepo.getPart(partId) }
                .subscribe { part -> newState { copy(uri = part.getUri(), type = part.type) } }

        disposables += partIdFlowable
                .mapNotNull { partId -> messageRepo.getMessageForPart(partId) }
                .mapNotNull { message -> conversationRepo.getConversation(message.threadId) }
                .subscribe { conversation -> newState { copy(title = conversation.getTitle()) } }
    }

    override fun bindView(view: GalleryView) {
        super.bindView(view)

        // When the screen is touched, toggle the visibility of the navigation UI
        view.screenTouchedIntent
                .withLatestFrom(state) { _, state -> state.navigationVisible }
                .map { navigationVisible -> !navigationVisible }
                .autoDisposable(view.scope())
                .subscribe { navigationVisible -> newState { copy(navigationVisible = navigationVisible) } }

        // Save image to device
        view.optionsItemSelectedIntent
                .filter { itemId -> itemId == R.id.save }
                .withLatestFrom(partIdFlowable.toObservable()) { _, partId -> partId }
                .autoDisposable(view.scope())
                .subscribe { partId -> saveImage.execute(partId) { context.makeToast(R.string.gallery_toast_saved) } }
    }

}