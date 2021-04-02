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

package com.moez.QKSMS.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.getColorCompat
import com.moez.QKSMS.feature.compose.ComposeActivity
import com.moez.QKSMS.feature.main.MainActivity
import com.moez.QKSMS.manager.WidgetManager
import com.moez.QKSMS.util.Preferences
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class WidgetProvider : AppWidgetProvider() {

    @Inject lateinit var colors: Colors
    @Inject lateinit var prefs: Preferences

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        when (intent.action) {
            WidgetManager.ACTION_NOTIFY_DATASET_CHANGED -> updateData(context)
            else -> super.onReceive(context, intent)
        }
    }

    /**
     * Update all widgets in the list
     */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        updateData(context)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetId, isSmallWidget(appWidgetManager, appWidgetId))
        }
    }

    /**
     * Notify all the widgets that they should update their adapter data
     */
    private fun updateData(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))

        // We need to update all Mms appwidgets on the home screen.
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.conversations)
    }

    /**
     * Update widget when widget size changes
     */
    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        updateWidget(context, appWidgetId, isSmallWidget(appWidgetManager, appWidgetId))
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    /**
     * Returns true when widget has less than 4 columns, else false
     */
    private fun isSmallWidget(appWidgetManager: AppWidgetManager, appWidgetId: Int): Boolean {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val size = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

        var n = 2
        while (70 * n - 30 < size) {
            ++n
        }

        val columns = n - 1

        return columns < 4
    }

    /**
     * Update the widget appWidgetId
     */
    private fun updateWidget(context: Context, appWidgetId: Int, smallWidget: Boolean) {
        Timber.v("updateWidget appWidgetId: $appWidgetId")
        val remoteViews = RemoteViews(context.packageName, R.layout.widget)

        // Apply colors from theme
        val night = prefs.night.get()
        val black = prefs.black.get()

        remoteViews.setInt(R.id.background, "setColorFilter", context.getColorCompat(when {
            night && black -> R.color.black
            night && !black -> R.color.backgroundDark
            else -> R.color.white
        }))

        remoteViews.setInt(R.id.toolbar, "setColorFilter", context.getColorCompat(when {
            night && black -> R.color.black
            night && !black -> R.color.backgroundDark
            else -> R.color.backgroundLight
        }))

        remoteViews.setTextColor(R.id.title, context.getColorCompat(when (night) {
            true -> R.color.textPrimaryDark
            false -> R.color.textPrimary
        }))

        remoteViews.setInt(R.id.compose, "setColorFilter", colors.theme().theme)

        // Set adapter for conversations
        val intent = Intent(context, WidgetService::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra("small_widget", smallWidget)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        remoteViews.setRemoteAdapter(R.id.conversations, intent)

        // Main intent
        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPI = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.title, mainPI)

        // Compose intent
        val composeIntent = Intent(context, ComposeActivity::class.java)
        val composePI = PendingIntent.getActivity(context, 0, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.compose, composePI)

        // Conversation intent
        val startActivityIntent = Intent(context, MainActivity::class.java)
        val startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setPendingIntentTemplate(R.id.conversations, startActivityPendingIntent)

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews)
    }

}
