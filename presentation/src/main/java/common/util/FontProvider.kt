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
package common.util

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import com.moez.QKSMS.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import util.Preferences
import util.extensions.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontProvider @Inject constructor(context: Context, prefs: Preferences) {

    val typeface: Observable<Optional<Typeface>> = prefs.systemFont.asObservable()
            .distinctUntilChanged()
            .switchMap { systemFont ->
                when (systemFont) {
                    true -> Observable.just(Optional(null))
                    false -> lato
                }
            }

    private val lato: Observable<Optional<Typeface>> = BehaviorSubject.create()

    init {
        ResourcesCompat.getFont(context, R.font.lato, object : ResourcesCompat.FontCallback() {
            override fun onFontRetrievalFailed(reason: Int) {
                Timber.w("Font retrieval failed: $reason")
            }

            override fun onFontRetrieved(typeface: Typeface) {
                val subject = lato as Subject<Optional<Typeface>>
                subject.onNext(Optional(typeface))
            }
        }, null)
    }

}