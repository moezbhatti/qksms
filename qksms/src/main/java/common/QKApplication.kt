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
package common

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.moez.QKSMS.BuildConfig
import common.di.AppComponentManager
import common.di.appComponent
import common.util.Analytics
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject


class QKApplication : Application() {

    /**
     * Inject this so that it is forced to initialize
     */
    @Suppress("unused")
    @Inject lateinit var analytics: Analytics

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY)

        AppComponentManager.init(this)
        appComponent.inject(this)

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .compactOnLaunch()
                .deleteRealmIfMigrationNeeded()
                .build())

        Timber.plant(Timber.DebugTree())
    }

}