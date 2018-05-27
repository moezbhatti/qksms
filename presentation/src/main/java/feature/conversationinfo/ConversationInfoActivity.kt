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
package feature.conversationinfo

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.base.QkThemedActivity
import common.util.extensions.scrapViews
import common.util.extensions.setVisible
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.conversation_info_activity.*
import javax.inject.Inject

class ConversationInfoActivity : QkThemedActivity(), ConversationInfoView {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var recipientAdapter: ConversationRecipientAdapter
    @Inject lateinit var mediaAdapter: ConversationMediaAdapter
    @Inject lateinit var itemDecoration: GridSpacingItemDecoration
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val notificationsIntent by lazy { notifications.clicks() }
    override val themeIntent by lazy { themePrefs.clicks() }
    override val archiveIntent by lazy { archive.clicks() }
    override val blockIntent by lazy { block.clicks() }
    override val deleteIntent by lazy { delete.clicks() }
    override val confirmDeleteIntent: PublishSubject<Unit> = PublishSubject.create()

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ConversationInfoViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.conversation_info_activity)
        showBackButton(true)
        viewModel.bindView(this)
        setTitle(R.string.info_title)

        mediaAdapter.thumbnailClicks
                .autoDisposable(scope())
                .subscribe { view -> navigator.showImageAnimated(this, view) }

        recipients.adapter = recipientAdapter

        media.adapter = mediaAdapter
        media.addItemDecoration(itemDecoration)

        theme
                .autoDisposable(scope())
                .subscribe { recipients.scrapViews() }
    }

    override fun render(state: ConversationInfoState) {
        if (state.hasError) {
            finish()
            return
        }

        threadId.onNext(state.threadId)
        recipientAdapter.threadId = state.threadId
        recipientAdapter.updateData(state.recipients)

        notifications.setVisible(!state.blocked)

        archive.setVisible(!state.blocked)
        archive.title = getString(when (state.archived) {
            true -> R.string.info_unarchive
            false -> R.string.info_archive
        })

        block.title = getString(when (state.blocked) {
            true -> R.string.info_unblock
            false -> R.string.info_block
        })

        mediaAdapter.updateData(state.media)
    }

    override fun showDeleteDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.button_delete, { _, _ -> confirmDeleteIntent.onNext(Unit) })
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

}