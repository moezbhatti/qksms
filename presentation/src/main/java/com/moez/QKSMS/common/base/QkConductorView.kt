package com.moez.QKSMS.common.base

import com.bluelinelabs.conductor.autodispose.ControllerEvent
import com.uber.autodispose.LifecycleScopeProvider

interface QkConductorView<in State> {

    fun render(state: State)

    fun scope(): LifecycleScopeProvider<ControllerEvent>

}