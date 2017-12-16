package com.moez.QKSMS.domain.interactor

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class Interactor<in Params, Result>: Disposable {

    private val disposables: CompositeDisposable = CompositeDisposable()

    abstract fun buildObservable(params: Params): Flowable<Result>

    fun execute(params: Params, observer: (Result) -> Unit = {}) {
        disposables.add(buildObservable(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer))
    }

    override fun dispose() {
        return disposables.dispose()
    }

    override fun isDisposed(): Boolean {
        return disposables.isDisposed
    }

}
