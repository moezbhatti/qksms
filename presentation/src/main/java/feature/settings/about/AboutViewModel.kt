package feature.settings.about

import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.base.QkViewModel
import injection.appComponent
import javax.inject.Inject

class AboutViewModel : QkViewModel<AboutView, Unit>(Unit) {

    @Inject lateinit var navigator: Navigator

    init {
        appComponent.inject(this)
    }

    override fun bindView(view: AboutView) {
        super.bindView(view)

        view.preferenceClickIntent
                .autoDisposable(view.scope())
                .subscribe { preference ->
                    when(preference.id) {
                        R.id.developer -> navigator.showDeveloper()

                        R.id.source -> navigator.showSourceCode()

                        R.id.changelog -> navigator.showChangelog()

                        R.id.contact -> navigator.showSupport()

                        R.id.license -> navigator.showLicense()
                    }
                }
    }

}