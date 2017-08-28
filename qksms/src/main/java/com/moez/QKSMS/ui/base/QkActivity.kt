package com.moez.QKSMS.ui.base

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.moez.QKSMS.QKApplication
import com.moez.QKSMS.dagger.AppComponent

abstract class QkActivity : AppCompatActivity(), LifecycleRegistryOwner {

    private val mRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return mRegistry
    }

    fun getAppComponent(): AppComponent? {
        return (application as QKApplication).appComponent
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}