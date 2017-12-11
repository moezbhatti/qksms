package com.moez.QKSMS.presentation.common.base

interface QkView<in State> {

    fun render(state: State)

}