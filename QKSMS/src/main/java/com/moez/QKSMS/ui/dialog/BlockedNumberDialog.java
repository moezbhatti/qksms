package com.moez.QKSMS.ui.dialog;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.BlockedConversationHelper;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.view.QKEditText;

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
}
