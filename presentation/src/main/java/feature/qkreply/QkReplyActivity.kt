/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package feature.qkreply

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkThemedActivity
import common.util.extensions.setBackgroundTint
import feature.compose.MessagesAdapter
import injection.appComponent
import kotlinx.android.synthetic.main.qkreply_activity.*
import javax.inject.Inject

class QkReplyActivity : QkThemedActivity<QkReplyViewModel>(), QkReplyView {

    override val viewModelClass = QkReplyViewModel::class

    @Inject lateinit var adapter: MessagesAdapter

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        setFinishOnTouchOutside(true)
        setContentView(R.layout.qkreply_activity)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        viewModel.bindView(this)

        colors.composeBackground
                .autoDisposable(scope())
                .subscribe { color -> background.setBackgroundTint(color) }

        toolbar.clipToOutline = true

        val layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        messages.layoutManager = layoutManager
        messages.adapter = adapter
    }

    override fun render(state: QkReplyState) {
        title = state.title

        adapter.data = state.data
    }

    override fun getAppThemeResourcesObservable() = colors.appDialogThemeResources

}