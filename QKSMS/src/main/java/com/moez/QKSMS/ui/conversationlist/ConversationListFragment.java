package com.moez.QKSMS.ui.conversationlist;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;


import com.google.i18n.phonenumbers.Phonenumber;
import com.melnykov.fab.FloatingActionButton;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.BlockedConversationHelper;
import com.moez.QKSMS.common.DialogHelper;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.utils.ColorUtils;
import com.moez.QKSMS.common.utils.PhoneNumberUtils;
import com.moez.QKSMS.common.vcard.ContactOperations;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.ContactHelper;
import com.moez.QKSMS.data.ContactList;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKFragment;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.compose.ComposeActivity;
import com.moez.QKSMS.ui.dialog.conversationdetails.ConversationDetailsDialog;
import com.moez.QKSMS.ui.messagelist.MessageListActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.AvatarView;
import com.moez.QKSMS.ui.view.QKTextView;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConversationListFragment extends QKFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        RecyclerCursorAdapter.ItemClickListener<Conversation>, RecyclerCursorAdapter.MultiSelectListener, Observer {

    public static final String TAG = "ConversationListFragment";

    @Bind(R.id.empty_state) View mEmptyState;
    @Bind(R.id.empty_state_icon) ImageView mEmptyStateIcon;
    @Bind(R.id.conversations_list) RecyclerView mRecyclerView;
    @Bind(R.id.fab) FloatingActionButton mFab;

    private ConversationListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ConversationDetailsDialog mConversationDetailsDialog;
    private SharedPreferences mPrefs;
    private MenuItem mBlockedItem;
    private boolean mShowBlocked = false;


    private boolean mViewHasLoaded = false;

    // This does not hold the current position of the list, rather the position the list is pending being set to
    private int mPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        setHasOptionsMenu(true);

        mAdapter = new ConversationListAdapter(mContext);
        mAdapter.setItemClickListener(this);
        mAdapter.setMultiSelectListener(this);
        mLayoutManager = new LinearLayoutManager(mContext);
        mConversationDetailsDialog = new ConversationDetailsDialog(mContext, getFragmentManager());

        LiveViewManager.registerView(QKPreference.THEME, this, key -> {
            if (!mViewHasLoaded) {
                return;
            }

            mFab.setColorNormal(ThemeManager.getColor());
            mFab.setColorPressed(ColorUtils.lighten(ThemeManager.getColor()));
            mFab.getDrawable().setColorFilter(ThemeManager.getTextOnColorPrimary(), PorterDuff.Mode.SRC_ATOP);

            mEmptyStateIcon.setColorFilter(ThemeManager.getTextOnBackgroundPrimary());
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations, null);
        ButterKnife.bind(this, view);

        mEmptyStateIcon.setColorFilter(ThemeManager.getTextOnBackgroundPrimary());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mFab.setColorNormal(ThemeManager.getColor());
        mFab.setColorPressed(ColorUtils.lighten(ThemeManager.getColor()));
        mFab.attachToRecyclerView(mRecyclerView);
        mFab.setColorFilter(ThemeManager.getTextOnColorPrimary());
        mFab.setOnClickListener(v -> {
            if (mAdapter.isInMultiSelectMode()) {
                mAdapter.disableMultiSelectMode(true);
            } else {
                mContext.startActivity(ComposeActivity.class);
            }
        });

        mViewHasLoaded = true;


        initLoaderManager();
        BlockedConversationHelper.FutureBlockedConversationObservable.getInstance().addObserver(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        resetSwipeAnimation();

    }


    /**
     * Returns the weighting for unread vs. read conversations that are selected, to decide
     * which options we should show in the multi selction toolbar
     */
    private int getUnreadWeight() {
        int unreadWeight = 0;
        for (Conversation conversation : mAdapter.getSelectedItems().values()) {
            unreadWeight += conversation.hasUnreadMessages() ? 1 : -1;
        }
        return unreadWeight;
    }

    /**
     * Returns the weighting for blocked vs. unblocked conversations that are selected
     */
    private int getBlockedWeight() {
        int blockedWeight = 0;
        for (Conversation conversation : mAdapter.getSelectedItems().values()) {
            blockedWeight += BlockedConversationHelper.isConversationBlocked(mPrefs, conversation.getThreadId()) ? 1 : -1;
        }
        return blockedWeight;
    }

    /**
     * Returns whether or not any of the selected conversations have errors
     */
    private boolean doSomeHaveErrors() {
        for (Conversation conversation : mAdapter.getSelectedItems().values()) {
            if (conversation.hasError()) {
                return true;
            }
        }
        return false;
    }

    public void inflateToolbar(Menu menu, MenuInflater inflater, Context context) {
        if (mAdapter.isInMultiSelectMode()) {
            inflater.inflate(R.menu.conversations_selection, menu);
            mContext.setTitle(getString(R.string.title_conversations_selected, mAdapter.getSelectedItems().size()));

            menu.findItem(R.id.menu_block).setVisible(mPrefs.getBoolean(SettingsFragment.BLOCKED_ENABLED, false));

            menu.findItem(R.id.menu_mark_read).setIcon(getUnreadWeight() >= 0 ? R.drawable.ic_mark_read : R.drawable.ic_mark_unread);
            menu.findItem(R.id.menu_mark_read).setTitle(getUnreadWeight() >= 0 ? R.string.menu_mark_read : R.string.menu_mark_unread);
            menu.findItem(R.id.menu_block).setTitle(getBlockedWeight() > 0 ? R.string.menu_unblock_conversations : R.string.menu_block_conversations);
            menu.findItem(R.id.menu_delete_failed).setVisible(doSomeHaveErrors());
        } else {
            inflater.inflate(R.menu.conversations, menu);
            mContext.setTitle(mShowBlocked ? R.string.title_blocked : R.string.title_conversation_list);

            mBlockedItem = menu.findItem(R.id.menu_blocked);
            BlockedConversationHelper.bindBlockedMenuItem(mContext, mPrefs, mBlockedItem, mShowBlocked);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_blocked:
                setShowingBlocked(!mShowBlocked);
                return true;

            case R.id.menu_delete:
                DialogHelper.showDeleteConversationsDialog((MainActivity) mContext, mAdapter.getSelectedItems().keySet());
                mAdapter.disableMultiSelectMode(true);
                return true;

            case R.id.menu_mark_read:
                for (long threadId : mAdapter.getSelectedItems().keySet()) {
                    if (getUnreadWeight() >= 0) {
                        new ConversationLegacy(mContext, threadId).markRead();
                    } else {
                        new ConversationLegacy(mContext, threadId).markUnread();
                    }
                }
                mAdapter.disableMultiSelectMode(true);
                return true;

            case R.id.menu_block:
                for (long threadId : mAdapter.getSelectedItems().keySet()) {
                    if (getBlockedWeight() > 0) {
                        BlockedConversationHelper.unblockConversation(mPrefs, threadId);
                    } else {
                        BlockedConversationHelper.blockConversation(mPrefs, threadId);
                    }
                }
                mAdapter.disableMultiSelectMode(true);
                initLoaderManager();
                return true;

            case R.id.menu_delete_failed:
                DialogHelper.showDeleteFailedMessagesDialog((MainActivity) mContext, mAdapter.getSelectedItems().keySet());
                mAdapter.disableMultiSelectMode(true);
                return true;

            case R.id.menu_done:
                mAdapter.disableMultiSelectMode(true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isShowingBlocked() {
        return mShowBlocked;
    }

    public void setShowingBlocked(boolean showBlocked) {
        mShowBlocked = showBlocked;
        mContext.setTitle(mShowBlocked ? R.string.title_blocked : R.string.title_conversation_list);
        BlockedConversationHelper.bindBlockedMenuItem(mContext, mPrefs, mBlockedItem, mShowBlocked);
        initLoaderManager();
    }

    @Override
    public void onItemClick(Conversation conversation, View view) {
        if (mAdapter.isInMultiSelectMode()) {
            mAdapter.toggleSelection(conversation.getThreadId(), conversation);
        } else {
            MessageListActivity.launch(mContext, conversation.getThreadId(), -1, null, true);
        }
    }

    @Override
    public void onItemLongClick(final Conversation conversation, View view) {
        mAdapter.toggleSelection(conversation.getThreadId(), conversation);
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
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BlockedConversationHelper.FutureBlockedConversationObservable.getInstance().deleteObserver(this);

        if (null == mRecyclerView) {
            return;
        }
        try {
            for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                View child = mRecyclerView.getChildAt(i);
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(child);




                if (holder instanceof ConversationListViewHolder) {
                    Contact.removeListener((ConversationListViewHolder) holder);
                }
            }
        } catch (Exception ignored) {
            //
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, SmsHelper.CONVERSATIONS_CONTENT_PROVIDER, Conversation.ALL_THREADS_PROJECTION,
                BlockedConversationHelper.getCursorSelection(mPrefs, mShowBlocked),
                BlockedConversationHelper.getBlockedConversationArray(mPrefs), "date DESC");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mAdapter != null) {
            // Swap the new cursor in.  (The framework will take care of closing the, old cursor once we return.)
            mAdapter.changeCursor(data);
            if (mPosition != 0) {
                mRecyclerView.scrollToPosition(Math.min(mPosition, data.getCount() - 1));
                mPosition = 0;
            }
            initSwipe();
        }

        mEmptyState.setVisibility(data != null && data.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
        }
    }

    @Override
    public void onMultiSelectStateChanged(boolean enabled) {
        mContext.invalidateOptionsMenu();
        mFab.setImageResource(enabled ? R.drawable.ic_accept : R.drawable.ic_add);
    }

    @Override
    public void onItemAdded(long id) {
        mContext.invalidateOptionsMenu();
    }

    @Override
    public void onItemRemoved(long id) {
        mContext.invalidateOptionsMenu();
    }

    /**
     * This should be called when there's a future blocked conversation, and it's received
     */
    @Override
    public void update(Observable observable, Object data) {
        initLoaderManager();
    }


    // ****** SwipeTransformer code ********


    //Resets the swiping animation effects - brings the RecycleView (a list item container in this app) back to normal
    private void resetSwipeAnimation(){
        getLoaderManager().restartLoader(0, null, this);
    }

    //This is called in onLoadFinished() and contains all the behavior for the swipe transformations
    private void initSwipe(){

        //An ItemTouchHelper permits us to respond to user input while using a RecycleView
        //Useful in a fragment, the alternatives require work-arounds
        ItemTouchHelper.Callback simpleItemTouchCallback = new ItemTouchHelper.Callback()  {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.DOWN|ItemTouchHelper.UP|
                        ItemTouchHelper.START|ItemTouchHelper.END;
                int swipeFlags = ItemTouchHelper.START|ItemTouchHelper.END|ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT;
                return makeMovementFlags(dragFlags,swipeFlags);
            }

            //This is called when a user stops interacting with an element and the animation is also finished
            @Override
            public void clearView(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // More clean-up code here if/when needed
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            //Called when the ViewHolder swiped or dragged by the ItemTouchHelper is changed.
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                   // User is not interacting with the screen
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();

                //If a full swipe to the left is detected (a conversation was selected)
                if (direction == ItemTouchHelper.LEFT){

                    //Call to unlock swiping again in the opposite direction
                    clearView(mRecyclerView,viewHolder);

                    // Programatically select the conversation to be deleted (no GUI involved)
                    //Method takes a threadId and an item position index
                    mAdapter.setSelected(mAdapter.getItem(position).getThreadId(),mAdapter.getItem(position));

                    //We show a confirm delete conversations dialog
                    DialogHelper.showDeleteConversationsDialog((MainActivity) mContext, mAdapter.getSelectedItems().keySet());
                    mAdapter.disableMultiSelectMode(true);

                    //Resets the swipe animation made (red with delete icon) and
                    // brings back to normal the affected view
                    resetSwipeAnimation();
                }

                else

                    //If a full swipe to the right is detected (a conversation was thus selected)
                    if (direction == ItemTouchHelper.RIGHT){

                        //Call to unlock swiping again in the opposite direction
                    clearView(mRecyclerView,viewHolder);

                    // Programmatically select the conversation (who's author we want to call)
                    mAdapter.setSelected(mAdapter.getItem(position).getThreadId(),mAdapter.getItem(position));
                        //We get the phone number data from the selected conversation
                    Collection<Conversation> swipedNumberData =  mAdapter.getSelectedItems().values();

                        //Converting the above Collection to a List so it can be easily manipulated
                    List intermediateNumber = Arrays.asList(swipedNumberData);
                        //Getting the number info - it's at the starting index
                    String swipedNumber = String.valueOf(intermediateNumber.get(0));

                    // Using a regex to extract the usable phone number
                        //Basically we want to extract all the is inside [ ], only the phone number satisfies this regex
                    Pattern p = Pattern.compile("\\[(.*?)\\]");
                        //We use a matcher to get a match ! After we get the match, we attribute it to our swipedNumber
                    Matcher m = p.matcher(swipedNumber);
                    if(m.find()) {
                        swipedNumber=m.group(1);
                    }

                    //We reset the selection made and the animation
                    mAdapter.setUnselected(mAdapter.getItem(position).getThreadId());
                    resetSwipeAnimation();

                    //We start the phone call with the extracted number
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                    phoneIntent.setData(Uri.parse("tel:"+swipedNumber));
                    startActivity(phoneIntent);

                }

            }

            //This is called by ItemTouchHelper on RecyclerView's onDraw callback.
            //If you would like to customize how your View's respond to user interactions, this is a good place to override.
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    //These fields are necessary for the transformations that follow
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    /*This code is continually executed while the user swipes right (dX>0) or left (dX<0)
                    While the user swipes right or left the mRecycleView is continually covered with a red or green rectangle (depending on swipe left or right)
                    An icon is also displayed (either for calling or deleting)
                    */
                    if(dX > 0){
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());

                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_phone_call);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                    else
                    if(dX < 0)
                    {
                            p.setColor(Color.parseColor("#D32F2F"));
                            RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());

                            c.drawRect(background, p);
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
                            RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                            c.drawBitmap(icon, null, icon_dest, p);
                    }
                }
                //Calling super
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        //creating and attaching the itemTouchHelper - it takes the previously constructed callback as a parameter
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

}
