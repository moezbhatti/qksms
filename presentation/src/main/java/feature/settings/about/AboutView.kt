package feature.settings.about

import common.base.QkView
import common.widget.PreferenceView
import io.reactivex.subjects.Subject

interface AboutView : QkView<Unit> {

    val preferenceClickIntent: Subject<PreferenceView>

}