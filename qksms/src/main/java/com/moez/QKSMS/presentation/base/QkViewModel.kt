package com.moez.QKSMS.presentation.base

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject

abstract class QkViewModel<View : QkView<State>, State>(initialState: State) : ViewModel() {

    val state = MutableLiveData<State>()
    var view: View? = null
        set(value) {
            field = value
            bindIntents()
        }

    private val stateReducer: PublishSubject<(State) -> State> = PublishSubject.create()

    init {
        stateReducer
                .scan(initialState, { previousState, reducer -> reducer(previousState) })
                .subscribe { newState -> state.value = newState }
    }

    fun bindIntents() {}

    protected fun newState(reducer: (State) -> State) {
        stateReducer.onNext(reducer)
    }

}