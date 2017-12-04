package com.moez.QKSMS.presentation.settings

data class SettingsState(
        val selectingTheme: Boolean = false,
        val syncing: Boolean = false,

        val isDefaultSmsApp: Boolean = false,
        val darkModeEnabled: Boolean = false,
        val autoEmojiEnabled: Boolean = true,
        val notificationsEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val deliveryEnabled: Boolean = false,
        val splitSmsEnabled: Boolean = false,
        val stripUnicodeEnabled: Boolean = false,
        val mmsEnabled: Boolean = true,
        val maxMmsSize: String = ""
)