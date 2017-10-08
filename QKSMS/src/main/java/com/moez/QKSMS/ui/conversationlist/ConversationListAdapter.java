package com.moez.QKSMS.ui.conversationlist;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.FontManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.emoji.EmojiRegistry;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class ConversationListAdapter extends RecyclerCursorAdapter<ConversationListViewHolder, Conversation> {


    private final SharedPreferences mPrefs;
    private boolean mAnimationsEnabled = true;

    public ConversationListAdapter(QKActivity context) {
        super(context);
        mPrefs = mContext.getPrefs();
    }

    protected Conversation getItem(int position) {
        mCursor.moveToPosition(position);
        return Conversation.from(mContext, mCursor);
    }

    @Override
    public ConversationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_conversation, null);

        ConversationListViewHolder holder = new ConversationListViewHolder(mContext, view);
        holder.mutedView.setImageResource(R.drawable.ic_notifications_muted);
        holder.unreadView.setImageResource(R.drawable.ic_unread_indicator);
        holder.errorIndicator.setImageResource(R.drawable.ic_error);

        LiveViewManager.registerView(QKPreference.THEME, this, key -> {
            holder.mutedView.setColorFilter(ThemeManager.getColor());
            holder.unreadView.setColorFilter(ThemeManager.getColor());
            holder.errorIndicator.setColorFilter(ThemeManager.getColor());
        });

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            holder.root.setBackgroundDrawable(ThemeManager.getRippleBackground());
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ConversationListViewHolder holder, int position) {
        final Conversation conversation = getItem(position);

        holder.mData = conversation;
        holder.mContext = mContext;
        holder.mClickListener = mItemClickListener;
        holder.root.setOnClickListener(holder);
        holder.root.setOnLongClickListener(holder);

        holder.mutedView.setVisibility(new ConversationPrefsHelper(mContext, conversation.getThreadId())
                .getNotificationsEnabled() ? View.GONE : View.VISIBLE);

        holder.errorIndicator.setVisibility(conversation.hasError() ? View.VISIBLE : View.GONE);

        final boolean hasUnreadMessages = conversation.hasUnreadMessages();
        if (hasUnreadMessages) {
            holder.unreadView.setVisibility(View.VISIBLE);
            holder.snippetView.setTextColor(ThemeManager.getTextOnBackgroundPrimary());
            holder.dateView.setTextColor(ThemeManager.getColor());
            holder.fromView.setType(FontManager.TEXT_TYPE_PRIMARY_BOLD);
            holder.snippetView.setMaxLines(5);
        } else {
            holder.unreadView.setVisibility(View.GONE);
            holder.snippetView.setTextColor(ThemeManager.getTextOnBackgroundSecondary());
            holder.dateView.setTextColor(ThemeManager.getTextOnBackgroundSecondary());
            holder.fromView.setType(FontManager.TEXT_TYPE_PRIMARY);
            holder.snippetView.setMaxLines(1);
        }

        LiveViewManager.registerView(QKPreference.THEME, this, key -> {
            holder.dateView.setTextColor(hasUnreadMessages ? ThemeManager.getColor() : ThemeManager.getTextOnBackgroundSecondary());
        });

        if (isInMultiSelectMode()) {
            holder.mSelected.setVisibility(View.VISIBLE);
            if (mAnimationsEnabled) {
                holder.expand();
            } else {
                holder.expandNoAnim();
            }
            if (isSelected(conversation.getThreadId())) {
                holder.mSelected.setImageResource(R.drawable.ic_selected);
                holder.mSelected.setColorFilter(ThemeManager.getColor());
                holder.mSelected.setAlpha(1f);
            } else {
                holder.mSelected.setImageResource(R.drawable.ic_unselected);
                holder.mSelected.setColorFilter(ThemeManager.getTextOnBackgroundSecondary());
                holder.mSelected.setAlpha(0.5f);
            }
        } else {
            if (mAnimationsEnabled) {
                holder.collapse();
            } else {
                holder.collapseNoAnim();
            }
            holder.mSelected.setVisibility(View.INVISIBLE);
        }

        LiveViewManager.registerView(QKPreference.HIDE_AVATAR_CONVERSATIONS, this, key -> {
            holder.mAvatarView.setVisibility(QKPreferences.getBoolean(QKPreference.HIDE_AVATAR_CONVERSATIONS) ? View.GONE : View.VISIBLE);
        });

        // Date
        holder.dateView.setText(DateFormatter.getConversationTimestamp(mContext, conversation.getDate()));

        // Subject
        String emojiSnippet = conversation.getSnippet();
        if (mPrefs.getBoolean(SettingsFragment.AUTO_EMOJI, false)) {
            emojiSnippet = EmojiRegistry.parseEmojis(emojiSnippet);
        }
        holder.snippetView.setText(emojiSnippet);

        Contact.addListener(holder);

        // Update the avatar and name
        holder.onUpdate(conversation.getRecipients().size() == 1 ? conversation.getRecipients().get(0) : null);
    }

    @Override
    public void setSelected(long threadId, Conversation object) {
        if (!mSelectedItems.containsKey(threadId)) {
            mSelectedItems.put(threadId, object);

            if (mMultiSelectListener != null) {
                mMultiSelectListener.onItemAdded(threadId);

                if (mSelectedItems.size() == 1) {
                    mMultiSelectListener.onMultiSelectStateChanged(true);
                }
            }
        }
    }

    @Override
    public void setUnselected(long threadId) {
        if (mSelectedItems.containsKey(threadId)) {
            mSelectedItems.remove(threadId);

            if (mMultiSelectListener != null) {
                mMultiSelectListener.onItemRemoved(threadId);

                if (mSelectedItems.size() == 0) {
                    mMultiSelectListener.onMultiSelectStateChanged(false);
                }
            }
        }
    }

    /**
     * Enable or disable animations for expand or collapse items on long click
     *
     * @param animationsEnabled true for enabling or false otherwise
     */
    public void setAnimationsEnabled(boolean animationsEnabled) {
        this.mAnimationsEnabled = animationsEnabled;
    }

    /**
     * Called when an items was long clicked
     *
     * @param conversation which was clicked
     * @param view         which was clicked
     */
    public void onItemLongClick(Conversation conversation, View view) {
        // if this is the only item selected then we need to play the animations
        if (mSelectedItems.size() == 1 && isSelected(conversation.getThreadId())) {
            notifyDataSetChanged();
        }
        onItemClick(conversation, view);
    }

    /**
     * Called when an item was clicked
     *
     * @param conversation which was clicked
     * @param view         which was clicked
     */
    public void onItemClick(Conversation conversation, View view) {
        ImageView chechmarkView = (ImageView) view.findViewById(R.id.selected);
        if (isSelected(conversation.getThreadId())) {
            chechmarkView.setImageResource(R.drawable.ic_selected);
            chechmarkView.setColorFilter(ThemeManager.getColor());
            chechmarkView.setAlpha(1f);
        } else {
            chechmarkView.setImageResource(R.drawable.ic_unselected);
            chechmarkView.setColorFilter(ThemeManager.getTextOnBackgroundSecondary());
            chechmarkView.setAlpha(0.5f);
        }

        if (mSelectedItems.size() == 0) {
            notifyDataSetChanged();
        }
    }
}
