package com.moez.QKSMS.ui.base

import com.hannesdorfmann.mosby.mvp.MvpFragment
import com.hannesdorfmann.mosby.mvp.MvpPresenter
import com.hannesdorfmann.mosby.mvp.MvpView

abstract class QkFragment<V : MvpView, P : MvpPresenter<V>> : MvpFragment<V, P>() {

}