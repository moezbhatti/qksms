package com.moez.QKSMS.feature.settings.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.R
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.widget.PreferenceView
import com.moez.QKSMS.injection.appComponent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.about_activity.view.*
import javax.inject.Inject

class AboutController : QkController(), AboutView {

    @Inject lateinit var presenter: AboutPresenter

    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()

    init {
        appComponent.inject(this)
        presenter.onCreate(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.about_activity, container, false).apply {
            version.summary = BuildConfig.VERSION_NAME

            // Listen to clicks for all of the preferences
            (0 until preferences.childCount)
                    .map { index -> preferences.getChildAt(index) }
                    .mapNotNull { view -> view as? PreferenceView }
                    .map { preference -> preference.clicks().map { preference } }
                    .let { preferences -> Observable.merge(preferences) }
                    .subscribe(preferenceClickIntent::onNext)
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.onAttach(this)
        setTitle(R.string.about_title)
        showBackButton(true)
    }

    override fun render(state: Unit) {
        // No special rendering required
    }


}