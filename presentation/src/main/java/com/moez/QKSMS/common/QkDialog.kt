package com.moez.QKSMS.common

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.common.util.extensions.setPadding
import com.moez.QKSMS.injection.appComponent
import javax.inject.Inject

/**
 * Wrapper around AlertDialog which makes it easier to display lists that use our UI
 */
class QkDialog @Inject constructor(private val context: Context, val adapter: MenuItemAdapter) {

    var title: String? = null

    init {
        appComponent.inject(this)
    }

    fun show(activity: Activity) {
        val recyclerView = RecyclerView(activity)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.setPadding(top = 8.dpToPx(context), bottom = 8.dpToPx(context))

        val dialog = AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(recyclerView)
                .create()

        val clicks = adapter.menuItemClicks
                .subscribe { dialog.dismiss() }

        dialog.setOnDismissListener {
            clicks.dispose()
        }

        dialog.show()
    }

    fun setTitle(@StringRes title: Int) {
        this.title = context.getString(title)
    }

}