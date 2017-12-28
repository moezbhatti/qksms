package com.moez.QKSMS.common.util

data class Optional<out T>(val value: T?) {
    fun notNull() = value != null
}