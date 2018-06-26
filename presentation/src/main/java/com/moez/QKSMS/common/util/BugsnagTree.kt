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