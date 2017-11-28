package com.moez.QKSMS.presentation.base

import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject

abstract class QkViewModel<in View : QkView<State>, State>(initialState: State) : ViewModel() {

    protected val state: BehaviorSubject<State> = BehaviorSubject.createDefault(initialState)

    protected val disposables = CompositeDisposable()
    protected val intents = CompositeDisposable()

    @CallSuper
    open fun bindView(view: View) {
        intents.clear()

        intents += state.subscribe { view.render(it) }
    }

    protected fun newState(reducer: (State) -> State) {
        state.value?.let { state.onNext(reducer(it)) }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
        intents.dispose()
    }

}