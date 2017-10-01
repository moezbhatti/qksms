package com.moez.QKSMS.domain.interactor

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

abstract class Interactor<T, in Params> {

    private val disposables: CompositeDisposable = CompositeDisposable()

    abstract fun buildUseCaseObservable(params: Params): Flowable<T>

    fun execute(observer: ((T) -> Unit), params: Params) {
        disposables.add(buildUseCaseObservable(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer))
    }

    fun dispose() = disposables.dispose()

}
