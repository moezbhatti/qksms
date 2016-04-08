package com.moez.QKSMS.ui.messagelist;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.base.QKActivity;

public class MessageListActivity extends QKActivity {

    public static final String ARG_THREAD_ID = "threadId";
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
        }

        MessageListFragment fragment = MessageListFragment.getInstance(mThreadId, mRowId, mHighlight, mShowImmediate);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out);
    }
}
