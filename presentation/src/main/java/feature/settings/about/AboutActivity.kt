package feature.settings.about

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.R
import common.base.QkThemedActivity
import common.widget.PreferenceView
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.about_activity.*
import javax.inject.Inject

class AboutActivity : QkThemedActivity(), AboutView {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[AboutViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)
        setTitle(R.string.about_title)
        showBackButton(true)
        viewModel.bindView(this)

        version.summary = BuildConfig.VERSION_NAME

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