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
package interactor

import android.content.Context
import android.provider.MediaStore
import androidx.net.toUri
import data.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SaveImage @Inject constructor(
        private val context: Context,
        private val messageRepo: MessageRepository
) : Interactor<Long>() {

    override fun buildObservable(params: Long): Flowable<*> {
        return Flowable.just(params)
                .map { partId -> messageRepo.getPart(partId) }
                .map { part -> part.image }
                .map { uriString -> uriString.toUri() }
                .map { uri -> MediaStore.Images.Media.getBitmap(context.contentResolver, uri) }
                .doOnNext { bitmap -> MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "title", "description") }
    }

}