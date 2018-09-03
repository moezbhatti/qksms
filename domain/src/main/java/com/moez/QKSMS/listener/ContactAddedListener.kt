package com.moez.QKSMS.listener

import io.reactivex.Single

interface ContactAddedListener {

    fun listen(address: String): Single<*>

}