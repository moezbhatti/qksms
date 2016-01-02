/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moez.QKSMS.ui.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.popup.QKComposeActivity;

public class WidgetProvider extends AppWidgetProvider {
    public static final String ACTION_NOTIFY_DATASET_CHANGED = "com.moez.QKSMS.intent.action.ACTION_NOTIFY_DATASET_CHANGED";

    private static final String TAG = "WidgetProvider";

    /**
     * Update all widgets in the list
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetId, isSmallWidget(appWidgetManager, appWidgetId));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive intent: " + intent);
        String action = intent.getAction();

        // The base class AppWidgetProvider's onReceive handles the normal widget intents. Here
        // we're looking for an intent sent by the messaging app when it knows a message has
        // been sent or received (or a conversation has been read) and is telling the widget it
        // needs to update.
        if (ACTION_NOTIFY_DATASET_CHANGED.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

            // We need to update all Mms appwidgets on the home screen.
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.conversation_list);
        } else {
            super.onReceive(context, intent);
        }
    }

    /**
     * Update widget when widget size changes
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        Log.v("TAG", "Changed widget dimensions");
        updateWidget(context, appWidgetId, isSmallWidget(appWidgetManager,appWidgetId));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * Returns 1 when widget has less than 4 columns, else 0
     */
    @TargetApi(16)
    private static int isSmallWidget(AppWidgetManager appWidgetManager, int appWidgetId) {

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int size = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        int columns = n - 1;

        if (columns < 4) return 1;
        else return 0;
    }

    /**
     * Update the widget appWidgetId
     */
    private static void updateWidget(Context context, int appWidgetId, int smallWidget) {
        Log.v(TAG, "updateWidget appWidgetId: " + appWidgetId);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        PendingIntent clickIntent;

        // Launch an intent to avoid ANRs
        final Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("small_widget", smallWidget);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.conversation_list, intent);

        remoteViews.setTextViewText(R.id.widget_label, context.getString(R.string.title_conversation_list));
        remoteViews.setTextColor(R.id.widget_label, ThemeManager.getTextOnColorPrimary());

        remoteViews.setInt(R.id.conversation_list_background, "setColorFilter", ThemeManager.getBackgroundColor());
        remoteViews.setInt(R.id.header_background, "setColorFilter", ThemeManager.getColor());

        // Open Mms's app conversation list when click on header
        final Intent convIntent = new Intent(context, MainActivity.class);
        clickIntent = PendingIntent.getActivity(context, 0, convIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_header, clickIntent);

        // On click intent for Compose
        final Intent composeIntent = new Intent(context, QKComposeActivity.class);
        clickIntent = PendingIntent.getActivity(context, 0, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_compose, clickIntent);

        // On click intent for Conversation
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.conversation_list, startActivityPendingIntent);

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews);
    }

    /*
     * notifyDatasetChanged call when the conversation list changes so the mms widget will
     * update and reflect the changes
     */
    public static void notifyDatasetChanged(Context context) {
        if (context != null) {
            Log.v(TAG, "notifyDatasetChanged");
            final Intent intent = new Intent(ACTION_NOTIFY_DATASET_CHANGED);
            context.sendBroadcast(intent);
        }
    }

    public static void notifyThemeChanged(Context context) {
        if (context != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetId, isSmallWidget(appWidgetManager, appWidgetId));
            }
        }

        notifyDatasetChanged(context);
    }

}
