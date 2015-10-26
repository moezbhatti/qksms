package com.moez.QKSMS.ui.popup;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.moez.QKSMS.R;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.base.QKPopupActivity;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.conversationlist.ConversationListAdapter;
import com.moez.QKSMS.ui.view.WrappingLinearLayoutManager;

public class MessagePickerActivity extends QKPopupActivity implements LoaderManager.LoaderCallbacks<Cursor>, RecyclerCursorAdapter.ItemClickListener {
    private final String TAG = "MessagePickerActivity";

    private RecyclerView mRecyclerView;
    private WrappingLinearLayoutManager mLayoutManager;
    private ConversationListAdapter mAdapter;
    private boolean mUnreadOnly = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_picker);

        mLayoutManager = new WrappingLinearLayoutManager(this);
        mAdapter = new ConversationListAdapter(this);
        mAdapter.setItemClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.conversation_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        initLoaderManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_picker, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_fold:
                switchView();
                if (mUnreadOnly) {
                    item.setIcon(R.drawable.ic_unfold);
                    item.setTitle(R.string.menu_show_all);
                } else {
                    item.setIcon(R.drawable.ic_fold);
                    item.setTitle(R.string.menu_show_unread);
                }
                return true;
        }

        return false;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_message_picker;
    }

    private void switchView() {
        mUnreadOnly = !mUnreadOnly;
        getLoaderManager().restartLoader(0, null, this);
    }

    private void initLoaderManager() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, SmsHelper.CONVERSATIONS_CONTENT_PROVIDER, Conversation.ALL_THREADS_PROJECTION,
                mUnreadOnly ? SmsHelper.UNREAD_SELECTION : null, null, SmsHelper.sortDateDesc);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the, old cursor once we return.)
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onItemClick(Object object, View view) {
        Conversation conversation = (Conversation) object;

        Intent intent = new Intent(this, QKReplyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(QKReplyActivity.EXTRA_THREAD_ID, conversation.getThreadId());
        intent.putExtra(QKReplyActivity.EXTRA_SHOW_KEYBOARD, true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemLongClick(Object object, View view) {

    }
}
