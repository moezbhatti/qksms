package com.moez.QKSMS.common.base

import androidx.annotation.CallSuper
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

abstract class QkPresenter<View : QkViewContract<State>, State>(initialState: State) {

    val disposables = CompositeDisposable()

    protected val state: BehaviorSubject<State> = BehaviorSubject.createDefault(initialState)

    @CallSuper
    open fun bindIntents(view: View) {
        state
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(view.scope())
                .subscribe(view::render)
    }

    protected fun newState(reducer: State.() -> State) {
        state.value?.let { state.onNext(reducer(it)) }
    }

    open fun onCleared() {
        disposables.dispose()
    }

}