package com.moez.QKSMS.ui.conversationlist;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.FontManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.ThemeManager;
import com.moez.QKSMS.common.emoji.EmojiRegistry;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;

public class ConversationListAdapter extends RecyclerCursorAdapter<ConversationListViewHolder, Conversation> {


    private final SharedPreferences mPrefs;

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
        holder.mMuted.setImageResource(R.drawable.ic_notifications_muted);
        holder.mError.setImageResource(R.drawable.ic_error);

        LiveViewManager.registerView(QKPreference.THEME, this, key -> {
            holder.mMuted.setColorFilter(ThemeManager.getColor());
            holder.mError.setColorFilter(ThemeManager.getColor());
        });

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            holder.itemView.setBackgroundDrawable(ThemeManager.getRippleBackground());
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ConversationListViewHolder holder, int position) {
        final Conversation conversation = getItem(position);

        holder.mData = conversation;
        holder.mContext = mContext;
        holder.mClickListener = mItemClickListener;
        holder.itemView.setOnClickListener(holder);
        holder.itemView.setOnLongClickListener(holder);

        holder.mMuted.setVisibility(new ConversationPrefsHelper(mContext, conversation.getThreadId())
                .getNotificationsEnabled() ? View.GONE : View.VISIBLE);

        holder.mError.setVisibility(conversation.hasError() ? View.VISIBLE : View.GONE);

        final boolean hasUnreadMessages = conversation.hasUnreadMessages();
        if (hasUnreadMessages) {
            holder.mSnippet.setTextColor(ThemeManager.getTextOnBackgroundPrimary());
            holder.mDate.setTextColor(ThemeManager.getColor());
            holder.mName.setType(FontManager.TEXT_TYPE_PRIMARY_BOLD);
            holder.mSnippet.setMaxLines(5);
        } else {
            holder.mSnippet.setTextColor(ThemeManager.getTextOnBackgroundSecondary());
            holder.mDate.setTextColor(ThemeManager.getTextOnBackgroundPrimary());
            holder.mName.setType(FontManager.TEXT_TYPE_PRIMARY);
            holder.mSnippet.setMaxLines(1);
        }

        if (isInMultiSelectMode()) {
            holder.mSelected.setVisibility(View.VISIBLE);
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
            holder.mSelected.setVisibility(View.GONE);
        }

        LiveViewManager.registerView(QKPreference.HIDE_AVATAR_CONVERSATIONS, this, key -> {
            holder.mAvatar.setVisibility(QKPreferences.getBoolean(QKPreference.HIDE_AVATAR_CONVERSATIONS) ? View.GONE : View.VISIBLE);
        });

        // Date
        holder.mDate.setText(DateFormatter.getConversationTimestamp(mContext, conversation.getDate()));

        // Subject
        String emojiSnippet = conversation.getSnippet();
        if (QKPreferences.getBoolean(QKPreference.AUTO_EMOJI)) {
            emojiSnippet = EmojiRegistry.parseEmojis(emojiSnippet);
        }
        holder.mSnippet.setText(emojiSnippet);

        Contact.addListener(holder);

        // Update the avatar and name
        holder.onUpdate(conversation.getRecipients().size() == 1 ? conversation.getRecipients().get(0) : null);
    }
}
