/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package common.widget

import android.content.Context
import android.graphics.Typeface
import android.support.text.emoji.widget.EmojiAppCompatTextView
import android.util.AttributeSet
import com.moez.QKSMS.R
import com.uber.autodispose.android.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.util.Colors
import common.util.FontProvider
import common.util.extensions.getColorCompat
import injection.appComponent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import util.Preferences
import javax.inject.Inject

open class QkTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : EmojiAppCompatTextView(context, attrs) {

    companion object {
        const val COLOR_PRIMARY = 0
        const val COLOR_SECONDARY = 1
        const val COLOR_TERTIARY = 2
        const val COLOR_PRIMARY_ON_THEME = 3
        const val COLOR_SECONDARY_ON_THEME = 4
        const val COLOR_TERTIARY_ON_THEME = 5
        const val COLOR_THEME = 6

        const val SIZE_PRIMARY = 0
        const val SIZE_SECONDARY = 1
        const val SIZE_TERTIARY = 2
        const val SIZE_TOOLBAR = 3
    }

    @Inject lateinit var colors: Colors
    @Inject lateinit var fontProvider: FontProvider
    @Inject lateinit var prefs: Preferences

    var textColorObservable: Observable<Int>? = null
        set(value) {
            field = value
            updateSubscription()
        }

    private var textColorDisposable: Disposable? = null
        set(value) {
            field?.dispose()
            field = value
        }

    private var textSizeAttrSubject: Subject<Int> = BehaviorSubject.create()

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        context.obtainStyledAttributes(attrs, R.styleable.QkTextView)?.run {
            val colorAttr = getInt(R.styleable.QkTextView_textColor, -1)
            val textSizeAttr = getInt(R.styleable.QkTextView_textSize, -1)

            if (isInEditMode) {
                setTextColor(context.getColorCompat(when (colorAttr) {
                    COLOR_PRIMARY -> R.color.textPrimary
                    COLOR_SECONDARY -> R.color.textSecondary
                    COLOR_TERTIARY -> R.color.textTertiary
                    COLOR_PRIMARY_ON_THEME -> R.color.textPrimaryDark
                    COLOR_SECONDARY_ON_THEME -> R.color.textSecondaryDark
                    COLOR_TERTIARY_ON_THEME -> R.color.textTertiaryDark
                    COLOR_THEME -> R.color.tools_theme
                    else -> R.color.textPrimary
                }))

                textSize = when (textSizeAttr) {
                    SIZE_PRIMARY -> 16f
                    SIZE_SECONDARY -> 14f
                    SIZE_TERTIARY -> 12f
                    SIZE_TOOLBAR -> 20f
                    else -> textSize
                }
            } else {
                textColorObservable = when (colorAttr) {
                    COLOR_PRIMARY -> colors.textPrimary
                    COLOR_SECONDARY -> colors.textSecondary
                    COLOR_TERTIARY -> colors.textTertiary
                    COLOR_PRIMARY_ON_THEME -> colors.textPrimaryOnTheme
                    COLOR_SECONDARY_ON_THEME -> colors.textSecondaryOnTheme
                    COLOR_TERTIARY_ON_THEME -> colors.textTertiaryOnTheme
                    COLOR_THEME -> colors.theme
                    else -> null
                }

                setTextSize(textSizeAttr)
            }

            recycle()
        }
    }

    /**
     * @see SIZE_PRIMARY
     * @see SIZE_SECONDARY
     * @see SIZE_TERTIARY
     * @see SIZE_TOOLBAR
     */
    fun setTextSize(size: Int) {
        textSizeAttrSubject.onNext(size)
    }

    private fun updateSubscription() {
        if (isAttachedToWindow) {
            textColorDisposable = textColorObservable?.subscribe { color ->
                setTextColor(color)
                setLinkTextColor(color)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        updateSubscription()

        fontProvider.typeface
                .autoDisposable(scope())
                .subscribe { setTypeface(it.value, typeface?.style ?: Typeface.NORMAL) }

        Observables
                .combineLatest(prefs.textSize.asObservable(), textSizeAttrSubject, { textSizePref, textSizeAttr ->
                    when (textSizeAttr) {
                        SIZE_PRIMARY -> textSize = when (textSizePref) {
                            Preferences.TEXT_SIZE_SMALL -> 14f
                            Preferences.TEXT_SIZE_NORMAL -> 16f
                            Preferences.TEXT_SIZE_LARGE -> 18f
                            Preferences.TEXT_SIZE_LARGER -> 20f
                            else -> 16f
                        }

                        SIZE_SECONDARY -> textSize = when (textSizePref) {
                            Preferences.TEXT_SIZE_SMALL -> 12f
                            Preferences.TEXT_SIZE_NORMAL -> 14f
                            Preferences.TEXT_SIZE_LARGE -> 16f
                            Preferences.TEXT_SIZE_LARGER -> 18f
                            else -> 14f
                        }

                        SIZE_TERTIARY -> textSize = when (textSizePref) {
                            Preferences.TEXT_SIZE_SMALL -> 10f
                            Preferences.TEXT_SIZE_NORMAL -> 12f
                            Preferences.TEXT_SIZE_LARGE -> 14f
                            Preferences.TEXT_SIZE_LARGER -> 16f
                            else -> 12f
                        }

                        SIZE_TOOLBAR -> textSize = when (textSizePref) {
                            Preferences.TEXT_SIZE_SMALL -> 18f
                            Preferences.TEXT_SIZE_NORMAL -> 20f
                            Preferences.TEXT_SIZE_LARGE -> 22f
                            Preferences.TEXT_SIZE_LARGER -> 26f
                            else -> 20f
                        }
                    }
                })
                .autoDisposable(scope())
                .subscribe()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        textColorDisposable?.dispose()
    }
}