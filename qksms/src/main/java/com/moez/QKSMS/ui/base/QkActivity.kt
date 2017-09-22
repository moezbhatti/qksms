package com.moez.QKSMS.ui.base

import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

abstract class QkActivity : AppCompatActivity() {

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