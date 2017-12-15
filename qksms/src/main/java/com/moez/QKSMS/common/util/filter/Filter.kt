package com.moez.QKSMS.common.util.filter

abstract class Filter<in T> {

    abstract fun filter(item: T, query: CharSequence): Boolean

}