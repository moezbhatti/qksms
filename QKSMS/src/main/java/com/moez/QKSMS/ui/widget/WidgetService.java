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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.Telephony.Threads;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.utils.ImageUtils;
import com.moez.QKSMS.common.utils.MessageUtils;
import com.moez.QKSMS.data.ContactHelper;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;

public class WidgetService extends RemoteViewsService {
    private static final String TAG = "WidgetService";

    /**
     * Lock to avoid race condition between widgets.
     */
    private static final Object sWidgetLock = new Object();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.v(TAG, "onGetViewFactory intent: " + intent);
        return new WidgetFactory(getApplicationContext(), intent);
    }

    /**
     * Remote Views Factory for Mms Widget.
     */
    private static class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {
        private static final int MAX_CONVERSATIONS_COUNT = 25;
        private final Context mContext;
        private final int mAppWidgetId;
        private final int mSmallWidget;
        private boolean mShouldShowViewMore;
        private Cursor mConversationCursor;
        private int mUnreadConvCount;
        private final AppWidgetManager mAppWidgetManager;

        public WidgetFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            mSmallWidget = intent.getIntExtra("small_widget", 0);
            mAppWidgetManager = AppWidgetManager.getInstance(context);
            Log.v(TAG, "WidgetFactory intent: " + intent + "widget id: " + mAppWidgetId);
        }

        @Override
        public void onCreate() {
            Log.v(TAG, "onCreate");
        }

        @Override
        public void onDestroy() {
            Log.v(TAG, "onDestroy");
            synchronized (sWidgetLock) {
                if (mConversationCursor != null && !mConversationCursor.isClosed()) {
                    mConversationCursor.close();
                    mConversationCursor = null;
                }
            }
        }

        @Override
        public void onDataSetChanged() {
            Log.v(TAG, "onDataSetChanged");
            synchronized (sWidgetLock) {
                if (mConversationCursor != null) {
                    mConversationCursor.close();
                    mConversationCursor = null;
                }
                mConversationCursor = queryAllConversations();
                mUnreadConvCount = queryUnreadCount();
                onLoadComplete();
            }
        }

        private Cursor queryAllConversations() {
            return mContext.getContentResolver().query(Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION,
                    null, null, null);
        }

        private int queryUnreadCount() {
            Cursor cursor = null;
            int unreadCount = 0;
            try {
                cursor = mContext.getContentResolver().query(Conversation.sAllThreadsUri, Conversation.ALL_THREADS_PROJECTION,
                        Threads.READ + "=0", null, null);
                if (cursor != null) {
                    unreadCount = cursor.getCount();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return unreadCount;
        }

        /**
         * Returns the number of items should be shown in the widget list.  This method also updates
         * the boolean that indicates whether the "show more" item should be shown.
         *
         * @return the number of items to be displayed in the list.
         */
        @Override
        public int getCount() {
            Log.v(TAG, "getCount");
            synchronized (sWidgetLock) {
                if (mConversationCursor == null) {
                    return 0;
                }
                final int count = getConversationCount();
                mShouldShowViewMore = count < mConversationCursor.getCount();
                return count + (mShouldShowViewMore ? 1 : 0);
            }
        }

        /**
         * Returns the number of conversations that should be shown in the widget.  This method
         * doesn't update the boolean that indicates that the "show more" item should be included
         * in the list.
         *
         * @return
         */
        private int getConversationCount() {
            Log.v(TAG, "getConversationCount");

            return Math.min(mConversationCursor.getCount(), MAX_CONVERSATIONS_COUNT);
        }

        /*
         * Add color to a given text
         */
        private SpannableStringBuilder addColor(CharSequence text, int color) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            if (color != 0) {
                builder.setSpan(new ForegroundColorSpan(color), 0, text.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return builder;
        }

        /**
         * @return the {@link RemoteViews} for a specific position in the list.
         */
        @Override
        public RemoteViews getViewAt(int position) {
            Log.v(TAG, "getViewAt position: " + position);
            synchronized (sWidgetLock) {
                // "View more conversations" view.
                if (mConversationCursor == null || (mShouldShowViewMore && position >= getConversationCount())) {
                    return getViewMoreConversationsView();
                }

                if (!mConversationCursor.moveToPosition(position)) {
                    // If we ever fail to move to a position, return the "View More conversations" view.
                    Log.w(TAG, "Failed to move to position: " + position);
                    return getViewMoreConversationsView();
                }

                Conversation conversation = Conversation.from(mContext, mConversationCursor);
                RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.list_item_conversation_widget);
                if (mSmallWidget == 0) {
                    remoteViews.setViewVisibility(R.id.avatar, View.VISIBLE);
                    bindAvatar(remoteViews, conversation);
                }
                else remoteViews.setViewVisibility(R.id.avatar, View.GONE);
                bindIndicators(remoteViews, conversation);
                bindDate(remoteViews, conversation);
                bindName(remoteViews, conversation);
                bindSnippet(remoteViews, conversation);

                // On click intent.
                Intent clickIntent = new Intent();
                clickIntent.putExtra(MainActivity.EXTRA_THREAD_ID, conversation.getThreadId());
                remoteViews.setOnClickFillInIntent(R.id.conversation, clickIntent);

                return remoteViews;
            }
        }

        private void bindAvatar(RemoteViews remoteViews, Conversation conversation) {

            Drawable avatar = conversation.getRecipients().get(0).getAvatar(mContext, null);
            Bitmap avatarBitmap = avatar != null ? ImageUtils.drawableToBitmap(avatar) : null;
            if (avatarBitmap == null) {
                avatarBitmap = ContactHelper.blankContact(mContext, conversation.getRecipients().formatNames(", "));
            }
            avatarBitmap = ImageUtils.getCircleBitmap(avatarBitmap, avatarBitmap.getWidth());
            remoteViews.setImageViewBitmap(R.id.avatar, avatarBitmap);
        }

        private void bindIndicators(RemoteViews remoteViews, Conversation conversation) {
            remoteViews.setInt(R.id.muted, "setColorFilter", ThemeManager.getColor());
            remoteViews.setInt(R.id.error, "setColorFilter", ThemeManager.getColor());
            remoteViews.setInt(R.id.unread, "setColorFilter", ThemeManager.getColor());
            remoteViews.setViewVisibility(R.id.muted, new ConversationPrefsHelper(mContext, conversation.getThreadId())
                    .getNotificationsEnabled() ? View.GONE : View.VISIBLE);
            remoteViews.setViewVisibility(R.id.error, conversation.hasError() ? View.VISIBLE : View.GONE);
            remoteViews.setViewVisibility(R.id.unread, conversation.hasUnreadMessages() ? View.VISIBLE : View.GONE);
        }

        private void bindName(RemoteViews remoteViews, Conversation conversation) {
            SpannableStringBuilder from = addColor(conversation.getRecipients().formatNames(", "), ThemeManager.getTextOnBackgroundPrimary());

            if (conversation.hasDraft()) {
                from.append(mContext.getResources().getString(R.string.draft_separator));
                int before = from.length();
                from.append(mContext.getResources().getString(R.string.has_draft));
                from.setSpan(new TextAppearanceSpan(mContext, android.R.style.TextAppearance_Small,
                        ThemeManager.getTextOnBackgroundPrimary()), before, from.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                from.setSpan(new ForegroundColorSpan(ThemeManager.getColor()), before, from.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            remoteViews.setTextViewText(R.id.name, from);
        }

        private void bindDate(RemoteViews remoteViews, Conversation conversation) {
            remoteViews.setTextViewText(R.id.date, addColor(MessageUtils.formatTimeStampString(mContext, conversation.getDate()),
                    conversation.hasUnreadMessages() ? ThemeManager.getColor() : ThemeManager.getTextOnBackgroundSecondary()));
        }

        private void bindSnippet(RemoteViews remoteViews, Conversation conversation) {
            remoteViews.setTextViewText(R.id.snippet, addColor(conversation.getSnippet(), conversation.hasUnreadMessages() ?
                    ThemeManager.getTextOnBackgroundPrimary() : ThemeManager.getTextOnBackgroundSecondary()));
        }

        /**
         * @return the "View more conversations" view. When the user taps this item, they're
         * taken to the messaging app's conversation list.
         */
        private RemoteViews getViewMoreConversationsView() {
            Log.v(TAG, "getViewMoreConversationsView");
            RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_loading);
            view.setTextViewText(R.id.loading_text, "View more conversations");
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                    new Intent(mContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.widget_loading, pendingIntent);
            return view;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_loading);
            view.setTextViewText(R.id.loading_text, mContext.getText(R.string.loading_conversations));
            return view;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void onLoadComplete() {
            Log.v(TAG, "onLoadComplete");
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget);

            mAppWidgetManager.partiallyUpdateAppWidget(mAppWidgetId, remoteViews);
        }

    }
}
