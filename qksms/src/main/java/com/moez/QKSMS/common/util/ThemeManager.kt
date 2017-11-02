package com.moez.QKSMS.common.util

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor() {

    val color = 0xFF008389.toInt()
    val bubbleColor = 0xFFF4F4F4.toInt()

    val textPrimary = 0xFF49555F.toInt()
    val textSecondary = 0xFF70808D.toInt()
    val textTertiary = 0xFFB7B9C0.toInt()

}