package com.moez.QKSMS.presentation.base

interface QkView<in State> {

    fun render(state: State)

}