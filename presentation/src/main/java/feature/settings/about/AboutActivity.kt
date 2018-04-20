package feature.settings.about

import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkThemedActivity
import common.widget.PreferenceView
import injection.appComponent
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.about_activity.*

class AboutActivity : QkThemedActivity<AboutViewModel>(), AboutView {

    override val viewModelClass = AboutViewModel::class
    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)
        setTitle(R.string.about_title)
        showBackButton(true)
        viewModel.bindView(this)

        version.summary = BuildConfig.VERSION_NAME

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> window.decorView.setBackgroundColor(color) }

        // Listen to clicks for all of the preferences
        (0 until preferences.childCount)
                .map { index -> preferences.getChildAt(index) }
                .mapNotNull { view -> view as? PreferenceView }
                .forEach { preference ->
                    preference.clicks().map { preference }.subscribe(preferenceClickIntent)
                }
    }

    override fun render(state: Unit) {
        // No special rendering required
    }


}