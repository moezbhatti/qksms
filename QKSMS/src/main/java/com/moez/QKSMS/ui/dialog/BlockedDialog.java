package com.moez.QKSMS.ui.dialog;

import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.BlockedConversationHelper;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.view.QKEditText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class BlockedDialog {

    public static void showNumbersDialog(final QKActivity context, final SharedPreferences prefs) {

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

    public static void showPatternsDialog(final QKActivity context, final SharedPreferences prefs) {

        makeDialog(
                context,
                R.string.title_block_pattern,
                R.string.pref_block_pattern,
                BlockedConversationHelper.getBlockedPatterns(prefs),
                value -> BlockedConversationHelper.blockPattern(prefs, value),
                value -> BlockedConversationHelper.unblockPattern(prefs, value),
                value -> {
                    if (BlockedConversationHelper.isValidPattern(value))
                        return true;
                    Toast.makeText(context, R.string.invalid_pattern, Toast.LENGTH_SHORT)
                         .show();
                    return false;
                }
        );
    }

    public static void showWordsDialog(final QKActivity context, final SharedPreferences prefs) {

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


    private interface ValueProcessor {
        boolean apply(String value);
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
                     .setNegativeButton(R.string.ret, null)
                     .show();
        };

        parentDialog.setContext(context)
                    .setTitle(parentDialogTitle)
                    .setItems(false, values_, onItemClick)
                    .setPositiveButton(false, R.string.add, onAdd)
                    .setNegativeButton(R.string.ret, null)
                    .show();
    }

}
