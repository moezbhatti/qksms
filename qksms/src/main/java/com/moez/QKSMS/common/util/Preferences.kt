package com.moez.QKSMS.common.util

import android.provider.Settings
import com.f2prateek.rx.preferences2.RxSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(rxPrefs: RxSharedPreferences) {

    val defaultSms = rxPrefs.getBoolean("defaultSms", false)
    val theme = rxPrefs.getInteger("theme", 0xFF008389.toInt())
    val dark = rxPrefs.getBoolean("dark", false)
    val autoEmoji = rxPrefs.getBoolean("autoEmoji", true)
    val notifications = rxPrefs.getBoolean("notifications", true)
    val vibration = rxPrefs.getBoolean("vibration", true)
    val ringtone = rxPrefs.getString("ringtone", Settings.System.DEFAULT_NOTIFICATION_URI.toString())
    val delivery = rxPrefs.getBoolean("delivery", false)
    val split = rxPrefs.getBoolean("split", false)
    val unicode = rxPrefs.getBoolean("unicode", false)
    val mms = rxPrefs.getBoolean("mms", true)
    val mmsSize = rxPrefs.getLong("mmsSize", -1)

}