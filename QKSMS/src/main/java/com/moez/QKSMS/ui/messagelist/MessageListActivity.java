package com.moez.QKSMS.ui.messagelist;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.DonationManager;
import com.moez.QKSMS.mmssms.Utils;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.QKSwipeBackActivity;

import java.net.URLDecoder;

public class MessageListActivity extends QKSwipeBackActivity {
    private final String TAG = "MessageListActivity";

    public static final String ARG_THREAD_ID = "thread_id";
    public static final String ARG_ROW_ID = "rowId";
    public static final String ARG_HIGHLIGHT = "highlight";
    public static final String ARG_SHOW_IMMEDIATE = "showImmediate";

    private long mThreadId;
    private long mRowId;
    private String mHighlight;
    private boolean mShowImmediate;

    public static void launch(QKActivity context, long threadId, long rowId, String pattern, boolean showImmediate) {
        Intent intent = new Intent(context, MessageListActivity.class);
        intent.putExtra(ARG_THREAD_ID, threadId);
        intent.putExtra(ARG_ROW_ID, rowId);
        intent.putExtra(ARG_HIGHLIGHT, pattern);
        intent.putExtra(ARG_SHOW_IMMEDIATE, showImmediate);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.slide_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        showBackButton(true);

        Intent intent = getIntent();
        if (intent != null) {
            mThreadId = intent.getLongExtra(ARG_THREAD_ID, -1);
            mRowId = intent.getLongExtra(ARG_ROW_ID, -1);
            mHighlight = intent.getStringExtra(ARG_HIGHLIGHT);
            mShowImmediate = intent.getBooleanExtra(ARG_SHOW_IMMEDIATE, false);

            if (mThreadId == -1 && intent.getData() != null) {
                String data = intent.getData().toString();
                String scheme = intent.getData().getScheme();

                String address = null;
                if (scheme.startsWith("smsto") || scheme.startsWith("mmsto")) {
                    address = data.replace("smsto:", "").replace("mmsto:", "");
                } else if (scheme.startsWith("sms") || (scheme.startsWith("mms"))) {
                    address = data.replace("sms:", "").replace("mms:", "");
                }

                mThreadId = Utils.getOrCreateThreadId(this, formatPhoneNumber(address));
            }
        }

        if (mThreadId != -1) {
            Log.v(TAG, "Opening thread: " + mThreadId);
            MessageListFragment fragment = MessageListFragment.getInstance(mThreadId, mRowId, mHighlight, mShowImmediate);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commitAllowingStateLoss();
        } else {
            // TODO log crashlytics event and show toast
        }
    }

    private String formatPhoneNumber(String address) {
        address = URLDecoder.decode(address);
        address = "" + Html.fromHtml(address);
        address = PhoneNumberUtils.formatNumber(address);
        return address;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_donate:
                DonationManager.getInstance(this).showDonateDialog();
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.scrollToFinishActivity();
    }
}
