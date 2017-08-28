package com.moez.QKSMS.ui.base

import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.moez.QKSMS.QKApplication
import com.moez.QKSMS.dagger.AppComponent

abstract class QkActivity : AppCompatActivity() {

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