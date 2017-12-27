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
package com.moez.QKSMS.presentation.setup

import android.content.Context
import android.provider.Telephony
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Permissions
import com.moez.QKSMS.presentation.common.base.QkViewModel
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class SetupViewModel : QkViewModel<SetupView, SetupState>(SetupState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var permissions: Permissions

    init {
        appComponent.inject(this)
    }

    override fun bindView(view: SetupView) {
        super.bindView(view)

        intents += view.activityResumedIntent
                .subscribe {
                    val isDefault = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

                    if (!permissions.hasSmsAndContacts()) view.requestPermissions()
                    else if (isDefault) view.finish()
                }

        intents += view.skipIntent.subscribe { view.finish() }

        intents += view.nextIntent.subscribe { view.requestDefaultSms() }
    }

}