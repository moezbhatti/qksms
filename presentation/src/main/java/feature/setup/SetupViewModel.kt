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
package feature.setup

import android.content.Context
import android.provider.Telephony
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import injection.appComponent
import manager.PermissionManager
import common.base.QkViewModel
import feature.setup.SetupState
import feature.setup.SetupView
import javax.inject.Inject

class SetupViewModel : QkViewModel<SetupView, SetupState>(SetupState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var permissions: PermissionManager

    init {
        appComponent.inject(this)
    }

    override fun bindView(view: SetupView) {
        super.bindView(view)

        view.activityResumedIntent
                .autoDisposable(view.scope())
                .subscribe {
                    val isDefault = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

                    if (!permissions.hasSmsAndContacts()) view.requestPermissions()
                    else if (isDefault) view.finish()
                }

        view.skipIntent
                .autoDisposable(view.scope())
                .subscribe { view.finish() }

        view.nextIntent
                .autoDisposable(view.scope())
                .subscribe { view.requestDefaultSms() }
    }

}