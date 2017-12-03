package com.moez.QKSMS.presentation.settings

data class SettingsState(
        val selectingTheme: Boolean = false,
        val syncing: Boolean = false,
        val isDefaultSmsApp: Boolean = false
)