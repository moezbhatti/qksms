package com.moez.QKSMS.common;

import android.util.Log;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.moez.QKSMS.BuildConfig;
import com.moez.QKSMS.R;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.model.ChangeModel;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.dialog.QKDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DialogHelper {
    private static final String TAG = "DialogHelper";

    public static void showDeleteConversationDialog(MainActivity context, long threadId) {
        Set<Long> threadIds = new HashSet<>();
        threadIds.add(threadId);
        showDeleteConversationsDialog(context, threadIds);
    }

    public static void showDeleteConversationsDialog(final MainActivity context, final Set<Long> threadIds) {
        new DefaultSmsHelper(context, R.string.not_default_delete).showIfNotDefault(null);

        Set<Long> threads = new HashSet<>(threadIds); // Make a copy so the list isn't reset when multi-select is disabled
        new QKDialog()
                .setContext(context)
                .setTitle(R.string.delete_conversation)
                .setMessage(context.getString(R.string.delete_confirmation, threads.size()))
                .setPositiveButton(R.string.yes, v -> {
                    Log.d(TAG, "Deleting threads: " + Arrays.toString(threads.toArray()));
                    Conversation.ConversationQueryHandler handler = new Conversation.ConversationQueryHandler(context.getContentResolver());
                    Conversation.startDelete(handler, 0, false, threads);
                    Conversation.asyncDeleteObsoleteThreads(handler, 0);
                    context.showMenu();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

    public static void showDeleteFailedMessagesDialog(final MainActivity context, final Set<Long> threadIds) {
        new DefaultSmsHelper(context, R.string.not_default_delete).showIfNotDefault(null);

        Set<Long> threads = new HashSet<>(threadIds); // Make a copy so the list isn't reset when multi-select is disabled
        new QKDialog()
                .setContext(context)
                .setTitle(R.string.delete_all_failed)
                .setMessage(context.getString(R.string.delete_all_failed_confirmation, threads.size()))
                .setPositiveButton(R.string.yes, v -> {
                    new Thread(() -> {
                        for (long threadId : threads) {
                            SmsHelper.deleteFailedMessages(context, threadId);
                        }
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void showChangelog(QKActivity context) {
        context.showProgressDialog();

        String url = "https://qksms-changelog.firebaseio.com/changes.json";

        StringRequest request = new StringRequest(url, response -> {
            Gson gson = new Gson();
            ChangeModel[] changes = gson.fromJson(response, ChangeModel[].class);

            // Fill in the localized date strings, and the `Long` time so that we can sort them
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateRevisionParser = new SimpleDateFormat("yyyy-MM-dd-'r'H"); // For multiple updates in a day
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM d, yyyy");
            for (ChangeModel change : changes) {
                try {
                    Date date;
                    if (change.getDate().length() > 11) {
                        date = dateRevisionParser.parse(change.getDate());
                    } else {
                        date = dateParser.parse(change.getDate());
                    }

                    change.setDate(dateFormatter.format(date));
                    change.setDateLong(date.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Arrays.sort(changes, (lhs, rhs) -> Long.valueOf(rhs.getDateLong()).compareTo(lhs.getDateLong()));

            // Only show changelogs for current and past versions
            boolean currentVersionReached = false;
            ArrayList<String> versions = new ArrayList<>();
            ArrayList<String> dates = new ArrayList<>();
            ArrayList<String> changelists = new ArrayList<>();
            for (ChangeModel change : changes) {
                if (change.getVersion().equals(BuildConfig.VERSION_NAME)) {
                    currentVersionReached = true;
                }

                if (currentVersionReached) {
                    versions.add(change.getVersion());
                    dates.add(change.getDate());

                    String changelist = "";
                    for (int i = 0; i < change.getChanges().size(); i++) {
                        String changeItem = change.getChanges().get(i);
                        changelist += " • ";
                        changelist += changeItem;
                        if (i < change.getChanges().size() - 1) {
                            changelist += "\n";
                        }
                    }
                    changelists.add(changelist);
                }
            }

            context.hideProgressDialog();

            new QKDialog()
                    .setContext(context)
                    .setTitle(R.string.title_changelog)
                    .setTripleLineItems(
                            versions.toArray(new String[versions.size()]),
                            dates.toArray(new String[versions.size()]),
                            changelists.toArray(new String[versions.size()]), null)
                    .show();
        }, error -> {
            context.hideProgressDialog();
            context.makeToast(R.string.toast_changelog_error);
        });

        context.getRequestQueue().add(request);
    }

}
