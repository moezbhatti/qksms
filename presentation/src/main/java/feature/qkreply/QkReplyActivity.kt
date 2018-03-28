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

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.base.QkThemedActivity
import common.util.extensions.autoScrollToStart
import common.util.extensions.setBackgroundTint
import feature.compose.MessagesAdapter
import injection.appComponent
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.qkreply_activity.*
import javax.inject.Inject

class QkReplyActivity : QkThemedActivity<QkReplyViewModel>(), QkReplyView {

    override val viewModelClass = QkReplyViewModel::class
    override val menuItemIntent: Subject<Int> = PublishSubject.create()

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

        colors.background
                .doOnNext { color -> background.setBackgroundTint(color) }
                .doOnNext { color -> composeGradient.setBackgroundTint(color) }
                .doOnNext { color -> composeBackground.setBackgroundTint(color) }
                .autoDisposable(scope())
                .subscribe()

        theme
                .autoDisposable(scope())
                .subscribe { color -> send.setBackgroundTint(color) }

        val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled))

        val iconEnabled = threadId
                .distinctUntilChanged()
                .switchMap { threadId -> colors.textPrimaryOnThemeForConversation(threadId) }

        val iconDisabled = threadId
                .distinctUntilChanged()
                .switchMap { threadId -> colors.textTertiaryOnThemeForConversation(threadId) }

        Observables
                .combineLatest(iconEnabled, iconDisabled, { primary, tertiary ->
                    ColorStateList(states, intArrayOf(primary, tertiary))
                })
                .autoDisposable(scope())
                .subscribe { tintList -> send.imageTintList = tintList }

        toolbar.clipToOutline = true

        adapter.autoScrollToStart(messages)

        messages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        messages.adapter = adapter
    }

    override fun render(state: QkReplyState) {
        threadId.onNext(state.data?.first?.id ?: 0)

        title = state.title

        toolbar.menu.findItem(R.id.expand)?.isVisible = !state.expanded
        toolbar.menu.findItem(R.id.collapse)?.isVisible = state.expanded

        adapter.data = state.data
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.qkreply, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        menuItemIntent.onNext(item.itemId)
        return true
    }

    override fun getAppThemeResourcesObservable() = colors.appDialogThemeResources

}