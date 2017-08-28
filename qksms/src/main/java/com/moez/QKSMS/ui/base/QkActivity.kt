package com.moez.QKSMS.ui.base

import android.support.v7.app.AppCompatActivity
import com.moez.QKSMS.QKApplication
import com.moez.QKSMS.dagger.AppComponent

abstract class QkActivity : AppCompatActivity() {

    fun getAppComponent(): AppComponent? {
        return (application as QKApplication).appComponent
    }

}