package com.moez.QKSMS.presentation.base

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

abstract class QkViewModel<in View : QkView<State>, State>(initialState: State) : ViewModel() {

    val state = MutableLiveData<State>()

    private val stateReducer: PublishSubject<(State) -> State> = PublishSubject.create()

    protected val disposables =  CompositeDisposable()
    protected val intents = CompositeDisposable()

    init {
        stateReducer
                .scan(initialState, { previousState, reducer -> reducer(previousState) })
                .subscribe { newState -> state.value = newState }
    }

    fun setView(view: View) {
        bindIntents(view)
    }

    @CallSuper
    open fun bindIntents(view: View) {
        intents.clear()
    }

    protected fun newState(reducer: (State) -> State) {
        stateReducer.onNext(reducer)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
        intents.dispose()
    }

}