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
package com.moez.QKSMS.feature.settings

import com.moez.QKSMS.repository.SyncRepository
import com.moez.QKSMS.util.Preferences

data class SettingsState(
    val theme: Int = 0,
    val nightModeSummary: String = "",
    val nightModeId: Int = Preferences.NIGHT_MODE_OFF,
    val nightStart: String = "",
    val nightEnd: String = "",
    val black: Boolean = false,
    val autoColor: Boolean = true,
    val autoEmojiEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val sendDelaySummary: String = "",
    val sendDelayId: Int = 0,
    val deliveryEnabled: Boolean = false,
    val signature: String = "",
    val textSizeSummary: String = "",
    val textSizeId: Int = Preferences.TEXT_SIZE_NORMAL,
    val systemFontEnabled: Boolean = false,
    val splitSmsEnabled: Boolean = false,
    val stripUnicodeEnabled: Boolean = false,
    val mobileOnly: Boolean = false,
    val autoDelete: Int = 0,
    val longAsMms: Boolean = false,
    val maxMmsSizeSummary: String = "100KB",
    val maxMmsSizeId: Int = 100,
    val syncProgress: SyncRepository.SyncProgress = SyncRepository.SyncProgress.Idle
)