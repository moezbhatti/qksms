package com.moez.QKSMS.ui.conversationlist;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.melnykov.fab.FloatingActionButton;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.DialogHelper;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.conversationdetails.ConversationDetailsDialog;
import com.moez.QKSMS.common.utils.ColorUtils;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.QKFragment;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.compose.ComposeFragment;
import com.moez.QKSMS.ui.dialog.ConversationNotificationSettingsDialog;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.settings.SettingsFragment;


public class ConversationListFragment extends QKFragment implements LoaderManager.LoaderCallbacks<Cursor>, LiveView,
        RecyclerCursorAdapter.ItemClickListener<Conversation>, RecyclerCursorAdapter.MultiSelectListener {

    private final String TAG = "ConversationList";

    private final int MENU_MUTE_CONVERSATION = 0;
    private final int MENU_UNMUTE_CONVERSATION = 1;
    private final int MENU_NOTIFICATION_SETTINGS = 2;
    private final int MENU_VIEW_DETAILS = 3;
    private final int MENU_MARK_READ = 4;
    private final int MENU_MARK_UNREAD = 5;
    private final int MENU_DELETE_FAILED = 6;
    private final int MENU_DELETE_CONVERSATION = 7;
    private final int MENU_MULTI_SELECT = 8;

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private ConversationListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ConversationDetailsDialog mConversationDetailsDialog;
    private QKActivity mContext;

    // This does not hold the current position of the list, rather the position the list is pending being set to
    private int mPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (QKActivity) getActivity();
        setHasOptionsMenu(true);

        mAdapter = new ConversationListAdapter(mContext);
        mAdapter.setItemClickListener(this);
        mAdapter.setMultiSelectListener(this);
        mLayoutManager = new LinearLayoutManager(mContext);
        mConversationDetailsDialog = new ConversationDetailsDialog(mContext, getFragmentManager());

        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.THEME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.conversations_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //TODO ListviewHelper.applyCustomScrollbar(mContext, mRecyclerView);

        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        mFab.setColorNormal(ThemeManager.getColor());
        mFab.setColorPressed(ColorUtils.lighten(ThemeManager.getColor()));
        mFab.attachToRecyclerView(mRecyclerView);
        mFab.getDrawable().setColorFilter(new PorterDuffColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.MULTIPLY));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the compose fragment, showing the keyboard and focusing on the recipients edittext.
                Bundle args = new Bundle();
                args.putBoolean(ComposeFragment.ARG_SHOW_KEYBOARD, true);
                args.putString(ComposeFragment.ARG_FOCUS, ComposeFragment.FOCUS_RECIPIENTS);

                Fragment content = getFragmentManager().findFragmentById(R.id.content_frame);
                switchFragment(ComposeFragment.getInstance(args, content));
            }
        });

        initLoaderManager();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Make sure we're only touching the conversation menu
        if (menu.findItem(R.id.menu_search) != null) {
            if (mAdapter.isInMultiSelectMode()) {
                menu.findItem(R.id.menu_search).setVisible(false);
                menu.findItem(R.id.menu_delete).setVisible(true);
                menu.findItem(R.id.menu_mark_read).setVisible(true);
            } else {
                menu.findItem(R.id.menu_search).setVisible(true);
                menu.findItem(R.id.menu_delete).setVisible(false);
                menu.findItem(R.id.menu_mark_read).setVisible(false);
            }
        } else {
            mAdapter.disableMultiSelectMode(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                DialogHelper.showDeleteConversationsDialog((MainActivity) mContext, mAdapter);
                return true;
            case R.id.menu_mark_read:
                for (long threadId : mAdapter.getSelectedItems()) {
                    new ConversationLegacy(mContext, threadId).markRead();
                }
                mAdapter.disableMultiSelectMode(true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Conversation conversation, View view) {
        if (mAdapter.isInMultiSelectMode()) {
            mAdapter.toggleSelection(conversation.getThreadId());
        } else {
            ((MainActivity) mContext).setConversation(conversation.getThreadId(), -1, null, true);
        }
    }

    @Override
    public void onItemLongClick(final Conversation conversation, View view) {
        if (mAdapter.isInMultiSelectMode()) {
            mAdapter.toggleSelection(conversation.getThreadId());
            return;
        }

        final long threadId = conversation.getThreadId();
        final String name = conversation.getRecipients().formatNames(", ");

        final ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(mContext, threadId);
        final boolean muted = !conversationPrefs.getNotificationsEnabled();

        final QKDialog dialog = new QKDialog()
                .setContext(mContext)
                .setTitle(name);

        if (!mAdapter.isInMultiSelectMode()) {
            dialog.addMenuItem(R.string.menu_multi_select, MENU_MULTI_SELECT);
        }

        if (muted) {
            dialog.addMenuItem(R.string.menu_unmute_conversation, MENU_UNMUTE_CONVERSATION);
        } else {
            dialog.addMenuItem(R.string.menu_mute_conversation, MENU_MUTE_CONVERSATION);
        }

        if (conversation.hasUnreadMessages()) {
            dialog.addMenuItem(R.string.menu_mark_read, MENU_MARK_READ);
        } else {
            dialog.addMenuItem(R.string.menu_mark_unread, MENU_MARK_UNREAD);
        }

        dialog.addMenuItem(R.string.menu_notification_settings, MENU_NOTIFICATION_SETTINGS);
        dialog.addMenuItem(R.string.menu_view_details, MENU_VIEW_DETAILS);

        if (conversation.hasError()) {
            dialog.addMenuItem(R.string.delete_all_failed, MENU_DELETE_FAILED);
        }

        dialog.addMenuItem(R.string.menu_delete_conversation, MENU_DELETE_CONVERSATION);

        dialog.buildMenu(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) id) {
                    case MENU_MUTE_CONVERSATION:
                        conversationPrefs.putBoolean(SettingsFragment.NOTIFICATIONS, false);
                        mAdapter.notifyDataSetChanged();
                        break;

                    case MENU_UNMUTE_CONVERSATION:
                        conversationPrefs.putBoolean(SettingsFragment.NOTIFICATIONS, true);
                        mAdapter.notifyDataSetChanged();
                        break;

                    case MENU_NOTIFICATION_SETTINGS:
                        ConversationNotificationSettingsDialog.newInstance(threadId, name).setContext(mContext)
                                .show(((MainActivity) mContext).getFragmentManager(), "notification prefs");
                        break;

                    case MENU_VIEW_DETAILS:
                        mConversationDetailsDialog.showDetails(conversation);
                        break;

                    case MENU_MARK_READ:
                        new ConversationLegacy(mContext, threadId).markRead();
                        break;

                    case MENU_MARK_UNREAD:
                        new ConversationLegacy(mContext, threadId).markUnread();
                        break;

                    case MENU_MULTI_SELECT:
                        mAdapter.setSelected(threadId);
                        break;

                    case MENU_DELETE_CONVERSATION:
                        DialogHelper.showDeleteConversationDialog((MainActivity) mContext, threadId);
                        break;
                    case MENU_DELETE_FAILED:
                        //Deletes all failed messages from all conversations
                        DialogHelper.showDeleteFailedMessagesDialog((MainActivity) mContext, threadId);
                        break;
                }
            }
        }).show(mContext.getFragmentManager(), "conversation options");
    }

    public void setPosition(int position) {
        mPosition = position;
        if (mLayoutManager != null && mAdapter != null) {
            mLayoutManager.scrollToPosition(Math.min(mPosition, mAdapter.getCount() - 1));
        }
    }

    public int getPosition() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private void initLoaderManager() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void switchFragment(Fragment fragment) {
        if (getActivity() == null) {
            return;
        }
        // TODO
        ((MainActivity) getActivity()).switchContent(fragment, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LiveViewManager.unregisterView(this);

    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        StringBuilder selection = new StringBuilder();
        selection.append(Telephony.Threads.MESSAGE_COUNT);
        selection.append(" != 0");  

        return new CursorLoader(mContext, SmsHelper.CONVERSATIONS_CONTENT_PROVIDER,
                Conversation.ALL_THREADS_PROJECTION, selection.toString(), null, "date DESC");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
            // Swap the new cursor in.  (The framework will take care of closing the, old cursor once we return.)
            mAdapter.changeCursor(data);
            if (mPosition != 0) {
                mRecyclerView.scrollToPosition(Math.min(mPosition, data.getCount() - 1));
                mPosition = 0;
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }

    @Override
    public void refresh() {
        if (mFab != null) {
            mFab.setColorNormal(ThemeManager.getColor());
            mFab.setColorPressed(ColorUtils.lighten(ThemeManager.getColor()));
            mFab.getDrawable().setColorFilter(new PorterDuffColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.MULTIPLY));
        }

        //TODO ListviewHelper.applyCustomScrollbar(mContext, mListView);
    }

    @Override
    public void onMultiSelectStateChanged(boolean enabled) {
        getActivity().invalidateOptionsMenu();
    }
}
