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
package com.moez.QKSMS.common.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.moez.QKSMS.common.androidxcompat.scope
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

abstract class QkViewModel<in View : QkView<State>, State>(initialState: State) : ViewModel() {

    protected val state: BehaviorSubject<State> = BehaviorSubject.createDefault(initialState)

    protected val disposables = CompositeDisposable()

    @CallSuper
    open fun bindView(view: View) {
        state
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(view.scope())
                .subscribe { view.render(it) }
    }

    protected fun newState(reducer: State.() -> State) {
        state.value?.let { state.onNext(reducer(it)) }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}