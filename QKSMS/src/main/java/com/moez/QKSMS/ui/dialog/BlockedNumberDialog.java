package com.moez.QKSMS.ui.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.BlockedConversationHelper;
import com.moez.QKSMS.common.utils.PhoneNumberUtils;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.view.QKEditText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlockedNumberDialog {

    public static void showDialog(final QKActivity context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> addresses = BlockedConversationHelper.getFutureBlockedConversations(prefs);

        new QKDialog()
                .setContext(context)
                .setTitle(R.string.pref_block_future)
                .setItems(addresses.toArray(new String[addresses.size()]), new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        new QKDialog()
                                .setContext(context)
                                .setTitle(R.string.title_unblock_address)
                                .setMessage(((TextView) view).getText().toString())
                                .setPositiveButton(R.string.yes, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        BlockedConversationHelper.unblockFutureConversation(prefs, ((TextView) view).getText().toString());
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                })
                .setPositiveButton(R.string.add, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final QKEditText editText = new QKEditText(context);
                        new QKDialog()
                                .setContext(context)
                                .setTitle(R.string.title_block_address)
                                .setCustomView(editText)
                                .setPositiveButton(R.string.add, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (editText.getText().length() > 0) {
                                            BlockedConversationHelper.blockFutureConversation(prefs, editText.getText().toString());
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void showNumbersDialog(final QKActivity context) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        makeDialog(
                context,
                R.string.title_block_address,
                R.string.pref_block_future,
                BlockedConversationHelper.getFutureBlockedConversations(prefs),
                value -> BlockedConversationHelper.blockFutureConversation(prefs, value),
                value -> BlockedConversationHelper.unblockFutureConversation(prefs, value),
                value -> true
        );
    }

    public static void showPatternsDialog(final QKActivity context) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        makeDialog(
                context,
                R.string.title_block_pattern,
                R.string.pref_block_pattern,
                BlockedConversationHelper.getBlockedPatterns(prefs),
                value -> BlockedConversationHelper.blockPattern(prefs, value),
                value -> BlockedConversationHelper.unblockPattern(prefs, value),
                value -> {
                    if (BlockedConversationHelper.compilePattern(value) != null)
                        return true;
                    toast(context, R.string.invalid_pattern);
                    return false;
                }
        );
    }

    public static void showWordsDialog(final QKActivity context) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        makeDialog(
                context,
                R.string.title_block_word,
                R.string.pref_block_word,
                BlockedConversationHelper.getBlockedWords(prefs),
                value -> BlockedConversationHelper.blockWord(prefs, value),
                value -> BlockedConversationHelper.unblockWord(prefs, value),
                value -> true
        );
    }

    public static void showNumberPrefixDialog(final QKActivity context) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        makeDialog(
                context,
                R.string.title_block_number_prefix,
                R.string.pref_block_number_prefix,
                BlockedConversationHelper.getBlockedNumberPrefixes(prefs),
                value -> BlockedConversationHelper.blockNumberPrefix(prefs, value),
                value -> BlockedConversationHelper.unblockNumberPrefix(prefs, value),
                value -> {
                    if (!PhoneNumberUtils.isWellFormedSmsAddress(value))
                        toast(context, R.string.invalid_number_prefix);
                    return true;
                }
        );
    }


    // TODO add a Function<T, R> interface, it's useful.
    // TODO is it ok to add guava dependency? if so it has the interface already.
    // java.util.function isn't available in android's sdk (java 7).
    private interface ValueProcessor {
        boolean apply(String value);
    }

    private static void toast(final Context context, @StringRes final int msg) {

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private static void makeDialog(final QKActivity context,
                                   @StringRes final int addDialogTitle,
                                   @StringRes final int parentDialogTitle,
                                   final Collection<String> values,
                                   final ValueProcessor adder,
                                   final ValueProcessor remover,
                                   final ValueProcessor validator) {

        final List<String> values_ = new ArrayList<>(values);
        Collections.sort(values_);

        final QKDialog parentDialog = new QKDialog();
        final QKDialog addDialog = new QKDialog();

        final AdapterView.OnItemClickListener onItemClick = (parent, view, position, id) -> {
            final CharSequence userInput = ((TextView) view).getText();
            final String value = userInput == null ? "" : userInput.toString();
            final boolean changed = remover.apply(value);
            if (changed) {
                values_.remove(value);
                parentDialog.getListAdapter().notifyDataSetChanged();
            }
        };

        final View.OnClickListener onAdd = view -> {
            final QKEditText text = new QKEditText(context);
            addDialog.setContext(context)
                    .setTitle(addDialogTitle)
                    .setCustomView(text)
                    .setPositiveButton(R.string.add, v -> {
                        final CharSequence userInput = ((TextView) text).getText();
                        final String value = userInput == null ? "" : userInput.toString();
                        if (value.isEmpty() || !validator.apply(value))
                            return;
                        final boolean changed = adder.apply(value);
                        if (changed) {
                            values_.add(value);
                            parentDialog.getListAdapter().notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        };

        parentDialog.setContext(context)
                .setTitle(parentDialogTitle)
                .setItems(false, values_, onItemClick)
                .setPositiveButton(false, R.string.add, onAdd)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
