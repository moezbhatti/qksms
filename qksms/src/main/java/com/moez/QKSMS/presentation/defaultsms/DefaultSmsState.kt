package com.moez.QKSMS.presentation.defaultsms

data class DefaultSmsState(
        val requestPermission: Boolean = false,
        val finished: Boolean = false
)