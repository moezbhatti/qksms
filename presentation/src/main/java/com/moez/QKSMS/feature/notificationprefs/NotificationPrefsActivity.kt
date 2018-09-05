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
package com.moez.QKSMS.feature.notificationprefs

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkThemedActivity
import com.moez.QKSMS.common.util.extensions.animateLayoutChanges
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.common.widget.PreferenceView
import com.uber.autodispose.kotlin.autoDisposable
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.notification_prefs_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

class NotificationPrefsActivity : QkThemedActivity(), NotificationPrefsView {

    @Inject lateinit var previewModeDialog: QkDialog
    @Inject lateinit var actionsDialog: QkDialog
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val previewModeSelectedIntent by lazy { previewModeDialog.adapter.menuItemClicks }
    override val ringtoneSelectedIntent: Subject<String> = PublishSubject.create()
    override val actionsSelectedIntent by lazy { actionsDialog.adapter.menuItemClicks }

    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[NotificationPrefsViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_prefs_activity)
        setTitle(R.string.title_notification_prefs)
        showBackButton(true)
        viewModel.bindView(this)

        preferences.postDelayed({ preferences?.animateLayoutChanges = true }, 100)

        val hasOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        notificationsO.setVisible(hasOreo)
        notifications.setVisible(!hasOreo)
        vibration.setVisible(!hasOreo)
        ringtone.setVisible(!hasOreo)

        previewModeDialog.setTitle(R.string.settings_notification_previews_title)
        previewModeDialog.adapter.setData(R.array.notification_preview_options)
        actionsDialog.adapter.setData(R.array.notification_actions)

        // Listen to clicks for all of the preferences
        (0 until preferences.childCount)
                .map { index -> preferences.getChildAt(index) }
                .mapNotNull { view -> view as? PreferenceView }
                .map { preference -> preference.clicks().map { preference } }
                .let { Observable.merge(it) }
                .autoDisposable(scope())
                .subscribe(preferenceClickIntent)
    }

    override fun render(state: NotificationPrefsState) {
        if (state.threadId != 0L) {
            title = state.conversationTitle
        }

        notifications.checkbox.isChecked = state.notificationsEnabled
        previews.summary = state.previewSummary
        previewModeDialog.adapter.selectedItem = state.previewId
        vibration.checkbox.isChecked = state.vibrationEnabled
        ringtone.summary = state.ringtoneName

        actionsDivider.isVisible = state.threadId == 0L
        actionsTitle.isVisible = state.threadId == 0L
        action1.isVisible = state.threadId == 0L
        action1.summary = state.action1Summary
        action2.isVisible = state.threadId == 0L
        action2.summary = state.action2Summary
        action3.isVisible = state.threadId == 0L
        action3.summary = state.action3Summary

        qkreplyDivider.isVisible = state.threadId == 0L
        qkreplyTitle.isVisible = state.threadId == 0L
        qkreply.checkbox.isChecked = state.qkReplyEnabled
        qkreply.isVisible = state.threadId == 0L
        qkreplyTapDismiss.setVisible(state.threadId == 0L && state.qkReplyEnabled)
        qkreplyTapDismiss.checkbox.isChecked = state.qkReplyTapDismiss
    }

    override fun showPreviewModeDialog() = previewModeDialog.show(this)

    override fun showRingtonePicker(default: Uri?) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, default)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        startActivityForResult(intent, 123)
    }

    override fun showActionDialog(selected: Int) {
        actionsDialog.adapter.selectedItem = selected
        actionsDialog.show(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            ringtoneSelectedIntent.onNext(uri?.toString() ?: "")
        }
    }

}