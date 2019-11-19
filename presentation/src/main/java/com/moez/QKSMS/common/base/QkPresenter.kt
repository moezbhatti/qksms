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
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class QkPresenter<View : QkViewContract<State>, State>(initialState: State) {

    protected val disposables = CompositeDisposable()
    protected val state: Subject<State> = BehaviorSubject.createDefault(initialState)

    private val stateReducer: Subject<State.() -> State> = PublishSubject.create()

    init {
        // If we accidentally push a realm object into the state on the wrong thread, switching
        // to mainThread right here should immediately alert us of the issue
        disposables += stateReducer
                .observeOn(AndroidSchedulers.mainThread())
                .scan(initialState) { state, reducer -> reducer(state) }
                .subscribe(state::onNext)
    }

    @CallSuper
    open fun bindIntents(view: View) {
        state
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(view.scope())
                .subscribe(view::render)
    }

    protected fun newState(reducer: State.() -> State) = stateReducer.onNext(reducer)

    open fun onCleared() = disposables.dispose()

}
