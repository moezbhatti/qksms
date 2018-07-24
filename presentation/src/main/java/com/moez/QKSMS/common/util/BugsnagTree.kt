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
package com.moez.QKSMS.common.util

import android.util.Log
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Severity
import timber.log.Timber

/**
 * If an exception is logged with the [Log.WARN] or [Log.ERROR] priority, the exception will be
 * sent to Bugsnag
 */
class BugsnagTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority == Log.WARN || priority == Log.ERROR
    }

    override fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?) {
        val severity = if (priority == Log.ERROR) Severity.ERROR else Severity.WARNING
        throwable?.run { Bugsnag.notify(this, severity) }
    }

}