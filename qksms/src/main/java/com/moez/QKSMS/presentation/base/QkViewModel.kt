package com.moez.QKSMS.presentation.base

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject

abstract class QkViewModel<State>(initialState: State) : ViewModel() {

    val state = MutableLiveData<State>()

    private val stateReducer: PublishSubject<(State) -> State> = PublishSubject.create()

    init {
        stateReducer
                .scan(initialState, { previousState, reducer -> reducer(previousState) })
                .subscribe { newState -> state.value = newState }
    }

    protected fun newState(reducer: (State) -> State) {
        stateReducer.onNext(reducer)
    }

}