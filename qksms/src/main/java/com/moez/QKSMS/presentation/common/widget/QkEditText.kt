package com.moez.QKSMS.presentation.common.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Colors
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

open class QkEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : EditText(context, attrs) {

    @Inject lateinit var colors: Colors

    private val disposables = CompositeDisposable()
    private var textColorObservable: Observable<Int>? = null
    private var textColorHintObservable: Observable<Int>? = null

    init {
        appComponent.inject(this)

        context.obtainStyledAttributes(attrs, R.styleable.QkEditText)?.run {
            textColorObservable = when (getInt(R.styleable.QkEditText_textColor, -1)) {
                0 -> colors.textPrimary
                1 -> colors.textSecondary
                2 -> colors.textTertiary
                3 -> colors.textPrimaryOnTheme
                4 -> colors.textSecondaryOnTheme
                5 -> colors.textTertiaryOnTheme
                else -> null
            }
            textColorHintObservable = when (getInt(R.styleable.QkEditText_textColorHint, -1)) {
                0 -> colors.textPrimary
                1 -> colors.textSecondary
                2 -> colors.textTertiary
                else -> null
            }
            recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        textColorObservable?.let { observable ->
            disposables += observable.subscribe { color -> setTextColor(color) }
        }

        textColorHintObservable?.let { observable ->
            disposables += observable.subscribe { color -> setHintTextColor(color) }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        disposables.dispose()
    }

}