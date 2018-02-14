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
package presentation.feature.gallery

import android.content.Intent
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.extensions.mapNotNull
import data.repository.MessageRepository
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import presentation.common.base.QkViewModel
import javax.inject.Inject

class GalleryViewModel(intent: Intent) : QkViewModel<GalleryView, GalleryState>(GalleryState()) {

    @Inject lateinit var messageRepo: MessageRepository

    init {
        appComponent.inject(this)

        val partIdFlowable = Flowable.just(intent)
                .map { it.getLongExtra("partId", 0L) }
                .filter { partId -> partId != 0L }

        disposables += partIdFlowable
                .mapNotNull { partId -> messageRepo.getPart(partId) }
                .mapNotNull { part -> part.image }
                .subscribe { path -> newState { it.copy(imagePath = path) } }

        disposables += partIdFlowable
                .mapNotNull { partId -> messageRepo.getMessageForPart(partId) }
                .mapNotNull { message -> messageRepo.getConversation(message.threadId) }
                .subscribe { conversation -> newState { it.copy(title = conversation.getTitle()) }  }
    }

    override fun bindView(view: GalleryView) {
        super.bindView(view)

        // When the screen is touched, toggle the visibility of the navigation UI
        view.screenTouchedIntent
                .withLatestFrom(state, { _, state -> state.navigationVisible })
                .map { navigationVisible -> !navigationVisible }
                .autoDisposable(view.scope())
                .subscribe { navigationVisible -> newState { it.copy(navigationVisible = navigationVisible) } }
    }

}