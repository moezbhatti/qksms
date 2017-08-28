package com.moez.QKSMS.ui.base

import android.arch.lifecycle.LifecycleFragment

abstract class QkFragment : LifecycleFragment() {

    override fun getContext(): QkActivity {
        return super.getContext() as QkActivity
    }

}