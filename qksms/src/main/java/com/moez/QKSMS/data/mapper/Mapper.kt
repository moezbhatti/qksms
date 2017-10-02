package com.moez.QKSMS.data.mapper

interface Mapper<in From, out To> {

    fun map(from: From): To

}