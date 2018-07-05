package com.moez.QKSMS.common.base

import androidx.annotation.CallSuper
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject

abstract class QkPresenter<View : QkConductorView<State>, State>(initialState: State) {

    protected val state: BehaviorSubject<State> = BehaviorSubject.createDefault(initialState)

    open fun onCreate(view: View) {
    }

    @CallSuper
    open fun onAttach(view: View) {
        state
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(view.scope())
                .subscribe(view::render)
    }

    protected fun newState(reducer: State.() -> State) {
        state.value?.let { state.onNext(reducer(it)) }
    }

}