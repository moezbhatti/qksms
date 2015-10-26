package com.moez.QKSMS.common;

import android.util.Log;
import android.view.View;
import com.moez.QKSMS.R;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.conversationlist.ConversationListAdapter;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.dialog.QKDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogHelper {
    private static final String TAG = "DialogHelper";

    public static void showDeleteConversationDialog(MainActivity context, long threadId) {
        List<Long> threadIds = new ArrayList<>();
        threadIds.add(threadId);
        showDeleteConversationDialog(context, threadIds);
    }

    public static void showDeleteFailedMessagesDialog(final MainActivity context, final long threadId) {
        new DefaultSmsHelper(context, R.string.not_default_delete).showIfNotDefault(null);

        new QKDialog()
                .setContext(context)
                .setTitle(R.string.delete_all_failed)
                .setMessage(R.string.delete_all_failed_confirmation)
                .setPositiveButton(R.string.yes, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            public void run() {
                                SmsHelper.deleteFailedMessages(context, threadId);
                            }
                        }).start();
                    }
                }).setNegativeButton(R.string.cancel, null)
                .show(context.getFragmentManager(), QKDialog.CONFIRMATION_TAG);
    }

    public static void showDeleteConversationDialog(final MainActivity context, final List<Long> threadIds) {

        new DefaultSmsHelper(context, R.string.not_default_delete).showIfNotDefault(null);

        new QKDialog()
                .setContext(context)
                .setTitle(R.string.delete_conversation)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Deleting threads: " + Arrays.toString(threadIds.toArray()));
                        Conversation.ConversationQueryHandler handler = new Conversation.ConversationQueryHandler(context.getContentResolver());

                        Conversation.startDelete(handler, 0, false, threadIds);
                        Conversation.asyncDeleteObsoleteThreads(handler, 0);
                        context.showMenu();
                    }
                }).setNegativeButton(R.string.cancel, null)
                .show(context.getFragmentManager(), QKDialog.CONFIRMATION_TAG);

    }

    public static void showDeleteConversationsDialog(MainActivity context, final ConversationListAdapter adapter) {

        new DefaultSmsHelper(context, R.string.not_default_delete).showIfNotDefault(null);


        final Conversation.ConversationQueryHandler handler = new Conversation.ConversationQueryHandler(context.getContentResolver());
        final ArrayList<Long> threadIds = adapter.getSelectedItems();

        new QKDialog()
                .setContext(context)
                .setTitle(R.string.delete_conversation)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Deleting threads: " + Arrays.toString(threadIds.toArray()));
                        Conversation.startDelete(handler, 0, false, threadIds);
                        Conversation.asyncDeleteObsoleteThreads(handler, 0);
                        adapter.disableMultiSelectMode(true);
                    }
                }).setNegativeButton(R.string.cancel, null)
                .show(context.getFragmentManager(), QKDialog.CONFIRMATION_TAG);
    }

}
