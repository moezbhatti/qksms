package com.moez.QKSMS.presentation.base

import android.arch.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.presentation.Navigator
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class QkActivity<VM : QkViewModel<*, *>> : AppCompatActivity() {

    @Inject lateinit var themeManager: ThemeManager

    protected abstract val viewModelClass: KClass<VM>
    protected val viewModel: VM by lazy {
        ViewModelProviders.of(this, Navigator.ViewModelFactory(intent))[viewModelClass.java]
    }

    protected var menu: Menu? = null
    protected var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)

        // Update the colours of the menu items
        disposables += themeManager.color.subscribe { color ->
            menu?.let { menu ->
                (0 until menu.size())
                        .map { position -> menu.getItem(position) }
                        .forEach { menuItem ->
                            menuItem.icon?.let { newIcon ->
                                newIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                                menuItem.icon = newIcon
                            }
                        }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

}