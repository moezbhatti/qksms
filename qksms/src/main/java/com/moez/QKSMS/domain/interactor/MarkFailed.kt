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
package com.moez.QKSMS.domain.interactor

import com.moez.QKSMS.data.repository.MessageRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class MarkFailed @Inject constructor(private val messageRepo: MessageRepository) : Interactor<MarkFailed.Params, Any>() {

    data class Params(val id: Long, val resultCode: Int)

    override fun buildObservable(params: Params): Flowable<Any> {
        return Flowable.just(Unit)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { messageRepo.markFailed(params.id, params.resultCode) }
    }

}