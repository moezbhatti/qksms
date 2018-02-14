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
package presentation.feature.setup

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.setup_activity.*
import presentation.common.Navigator
import presentation.common.base.QkThemedActivity
import javax.inject.Inject

class SetupActivity : QkThemedActivity<SetupViewModel>(), SetupView {

    override val viewModelClass = SetupViewModel::class
    override val activityResumedIntent: Subject<Unit> = PublishSubject.create()
    override val skipIntent by lazy { skip.clicks() }
    override val nextIntent by lazy { next.clicks() }

    @Inject lateinit var navigator: Navigator

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_activity)
        viewModel.bindView(this)

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> window.decorView.setBackgroundColor(color) }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(Unit)
    }

    override fun render(state: SetupState) {
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog()
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS), 0)
    }

}