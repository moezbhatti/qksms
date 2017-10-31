package com.moez.QKSMS.presentation.base

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.moez.QKSMS.presentation.Navigator
import kotlinx.android.synthetic.main.toolbar.*
import kotlin.reflect.KClass

abstract class QkActivity<VM : QkViewModel<*, State>, State> : AppCompatActivity(), QkView<State> {

    protected abstract val viewModelClass: KClass<VM>
    protected val viewModel: VM by lazy {
        ViewModelProviders.of(this, Navigator.ViewModelFactory(intent))[viewModelClass.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)

        viewModel.state.observe(this, Observer { it?.let { render(it) } })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setSupportActionBar(toolbar)
        title = title // The title may have been set before layout inflation
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        toolbarTitle?.text = title
    }

}