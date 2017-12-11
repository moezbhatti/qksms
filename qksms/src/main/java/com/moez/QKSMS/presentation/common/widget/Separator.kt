package com.moez.QKSMS.presentation.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Colors
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class Separator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    @Inject lateinit var colors: Colors

    private val disposables = CompositeDisposable()

    init {
        appComponent.inject(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        disposables += colors.separator.subscribe { color -> setBackgroundColor(color) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.dispose()
    }

}