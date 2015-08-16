package com.moez.QKSMS.ui.conversationlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.emoji.EmojiRegistry;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class ConversationListAdapter extends RecyclerCursorAdapter<ConversationListViewHolder, Conversation>
        implements LiveView {


    private final Resources mRes;
    private final SharedPreferences mPrefs;

    private final Drawable mMuted;
    private final Drawable mUnread;
    private final Drawable mError;

    public ConversationListAdapter(Context context) {
        mContext = context;
        mRes = MainActivity.getRes(mContext);
        mPrefs = MainActivity.getPrefs(mContext);

        mMuted = mRes.getDrawable(R.drawable.ic_mute);
        mUnread = mRes.getDrawable(R.drawable.ic_unread);
        mError = mRes.getDrawable(R.drawable.ic_error);

        LiveViewManager.registerView(this);
        LiveViewManager.registerPreference(this, SettingsFragment.THEME);
        refresh();
    }

    protected Conversation getItem(int position) {
        mCursor.moveToPosition(position);
        return Conversation.from(mContext, mCursor);
    }

    @Override
    public ConversationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_conversation, null);
        return new ConversationListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationListViewHolder holder, int position) {
        final Conversation conversation = getItem(position);

        holder.data = conversation;
        holder.context = mContext;
        holder.clickListener = mItemClickListener;
        holder.root.setOnClickListener(holder);
        holder.root.setOnLongClickListener(holder);

        // Have to clear the image drawable first, or else it won't reload it at all.
        holder.mutedView.setImageDrawable(null);
        holder.unreadView.setImageDrawable(null);
        holder.errorIndicator.setImageDrawable(null);
        holder.mutedView.setImageDrawable(mMuted);
        holder.unreadView.setImageDrawable(mUnread);
        holder.errorIndicator.setImageDrawable(mError);

        holder.mutedView.setVisibility(new ConversationPrefsHelper(mContext, conversation.getThreadId())
                .getNotificationsEnabled() ? View.GONE : View.VISIBLE);

        holder.errorIndicator.setVisibility(conversation.hasError() ? View.VISIBLE : View.GONE);

        if (conversation.hasUnreadMessages()) {
            holder.unreadView.setVisibility(View.VISIBLE);
            holder.snippetView.setTextColor(ThemeManager.getTextOnBackgroundPrimary());
            holder.dateView.setTextColor(ThemeManager.getColor());
        } else {
            holder.unreadView.setVisibility(View.GONE);
            holder.snippetView.setTextColor(ThemeManager.getTextOnBackgroundSecondary());
            holder.dateView.setTextColor(ThemeManager.getTextOnBackgroundSecondary());
        }

        if (isInMultiSelectMode()) {
            holder.mSelected.setVisibility(View.VISIBLE);
            if (isSelected(conversation.getThreadId())) {
                holder.mSelected.setImageResource(R.drawable.btn_radio_on);
                holder.mSelected.setColorFilter(ThemeManager.getColor());
            } else {
                holder.mSelected.setImageResource(R.drawable.btn_radio_off);
                holder.mSelected.setColorFilter(ThemeManager.getTextOnBackgroundSecondary());
            }
        } else {
            holder.mSelected.setVisibility(View.GONE);
        }

        if (mPrefs.getBoolean(SettingsFragment.HIDE_AVATAR_CONVERSATIONS, false)) {
            holder.mAvatarView.setVisibility(View.GONE);
        } else {
            holder.mAvatarView.setVisibility(View.VISIBLE);
        }

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
    public void refresh() {
        mMuted.setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.MULTIPLY);
        mUnread.setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.MULTIPLY);
        mError.setColorFilter(ThemeManager.getColor(), PorterDuff.Mode.MULTIPLY);
        notifyDataSetChanged();
    }
}
