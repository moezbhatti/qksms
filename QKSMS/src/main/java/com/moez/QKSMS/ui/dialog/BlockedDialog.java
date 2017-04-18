package com.moez.QKSMS.ui.dialog;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.BlockedConversationHelper;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.view.QKEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlockedDialog {

    private static String vText(final View view) {

        return ((TextView) view).getText().toString();
    }

    public static void showNumbersDialog(final QKActivity context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> addresses = new ArrayList<>(
                BlockedConversationHelper.getFutureBlockedConversations(prefs));
        Collections.sort(addresses);

        final QKDialog dialog = new QKDialog();

        dialog.setContext(context)
              .setTitle(R.string.pref_block_future)
              .setItems(false, addresses, new AdapterView.OnItemClickListener() {
                  @Override
                  public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                      final String address = vText(view);
                      final boolean removed = BlockedConversationHelper.unblockFutureConversation(prefs, address);
                      if (removed) {
                          addresses.remove(address);
                          dialog.getListAdapter().notifyDataSetChanged();
                      }
                  }
              })
              .setPositiveButton(R.string.add, new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      final QKEditText editText = new QKEditText(context);
                      dialog.setContext(context)
                            .setTitle(R.string.title_block_address)
                            .setCustomView(editText)
                            .setPositiveButton(R.string.add, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final String address = vText(editText);
                                    if (address.length() > 0) {
                                        final boolean added = BlockedConversationHelper.blockFutureConversation(prefs, address);
                                        if(added)
                                            dialog.getListAdapter().notifyDataSetChanged();
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

    public static void showPatternsDialog(final QKActivity context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final List<String> patterns = new ArrayList<>(BlockedConversationHelper.getBlockedPatterns(prefs));
        Collections.sort(patterns);

        final QKDialog dialog = new QKDialog();
        dialog.setContext(context)
              .setTitle(R.string.pref_block_future)
              .setItems(false, patterns, (parent, view, position, id) -> {
                  final String pattern =
                          vText(view);
                  final boolean removed =
                          BlockedConversationHelper.unblockPattern(prefs, pattern);
                  if (removed) {
                      patterns.remove(pattern);
                      dialog.getListAdapter().notifyDataSetChanged();
                  }
              })
              .setPositiveButton(false, R.string.add, v0 -> {
                  final QKEditText editText = new QKEditText(context);
                  new QKDialog()
                          .setContext(context)
                          .setTitle(R.string.title_block_pattern)
                          .setCustomView(editText)
                          .setPositiveButton(R.string.add, v1 -> {
                              final String pattern = vText(editText);
                              if (!BlockedConversationHelper.isValidPattern(pattern)) {
                                  Toast.makeText(context, "bad pattern!", Toast.LENGTH_SHORT).show();
                                  return;
                              }
                              final boolean add = BlockedConversationHelper.blockPattern(prefs, pattern);
                              if (add) {
                                  patterns.add(pattern);
                                  dialog.getListAdapter().notifyDataSetChanged();
                              }
                          })
                          .setNegativeButton(R.string.cancel, null)
                          .show();
              })
              .setNegativeButton(R.string.done, null)
              .show();
    }

    public static void showWordsDialog(final QKActivity context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final List<String> words = new ArrayList<>(BlockedConversationHelper.getBlockedWords(prefs));
        Collections.sort(words);

        final QKDialog dialog = new QKDialog();
        dialog.setContext(context)
              .setTitle(R.string.pref_block_future)
              .setItems(false, words, (parent, view, position, id) -> {
                  final String word = vText(view);
                  final boolean removed = BlockedConversationHelper.unblockWord(prefs, word);
                  if (removed) {
                      words.remove(word);
                      dialog.getListAdapter().notifyDataSetChanged();
                  }
              })
              .setPositiveButton(false, R.string.add, v -> {
                  final QKEditText editText = new QKEditText(context);
                  new QKDialog()
                          .setContext(context)
                          .setTitle(R.string.title_block_word)
                          .setCustomView(editText)
                          .setPositiveButton(R.string.add, v1 -> {
                              if (editText.getText().length() > 0) {
                                  final String word = editText.getText().toString();
                                  final boolean added = BlockedConversationHelper.blockWord(prefs, word);
                                  if (added) {
                                      words.add(word);
                                      dialog.getListAdapter().notifyDataSetChanged();
                                  }
                              }
                          })
                          .setNegativeButton(R.string.cancel, null)
                          .show();
              })
              .setNegativeButton(R.string.done, null)
              .show();
    }
}
