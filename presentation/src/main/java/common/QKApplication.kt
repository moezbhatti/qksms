package common

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
import android.app.Application
import android.support.text.emoji.EmojiCompat
import android.support.text.emoji.FontRequestEmojiCompatConfig
import android.support.v4.provider.FontRequest
import com.bugsnag.android.Bugsnag
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.R
import common.util.NightModeManager
import injection.AppComponentManager
import injection.appComponent
import io.realm.Realm
import io.realm.RealmConfiguration
import manager.AnalyticsManager
import timber.log.Timber
import javax.inject.Inject

class QKApplication : Application() {

    /**
     * Inject this so that it is forced to initialize
     */
    @Suppress("unused")
    @Inject lateinit var analyticsManager: AnalyticsManager

    @Inject lateinit var nightModeManager: NightModeManager

    override fun onCreate() {
        super.onCreate()

        Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY)
        Bugsnag.setAppVersion(BuildConfig.VERSION_NAME)

        AppComponentManager.init(this)
        appComponent.inject(this)

        nightModeManager.updateCurrentTheme()

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .compactOnLaunch()
                .deleteRealmIfMigrationNeeded()
                .build())

        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)

        EmojiCompat.init(FontRequestEmojiCompatConfig(this, fontRequest))

        Timber.plant(Timber.DebugTree())
    }

}