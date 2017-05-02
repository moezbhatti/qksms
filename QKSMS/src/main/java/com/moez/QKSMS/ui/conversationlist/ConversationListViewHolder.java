package com.moez.QKSMS.ui.conversationlist;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.ConversationLegacy;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.ClickyViewHolder;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.AvatarView;
import com.moez.QKSMS.ui.view.QKTextView;

import java.util.ArrayList;

public class ConversationListViewHolder extends ClickyViewHolder<Conversation> implements Contact.UpdateListener {

    private final SharedPreferences mPrefs;

    protected View root;
    protected QKTextView snippetView;
    protected QKTextView fromView;
    protected QKTextView dateView;
    protected ImageView mutedView;
    protected ImageView unreadView;
    protected ImageView errorIndicator;
    protected AvatarView mAvatarView;
    protected ImageView mSelected;

    public ConversationListViewHolder(QKActivity context, View view) {
        super(context, view);
        mPrefs = mContext.getPrefs();

        root = view;
        fromView = (QKTextView) view.findViewById(R.id.conversation_list_name);
        snippetView = (QKTextView) view.findViewById(R.id.conversation_list_snippet);
        dateView = (QKTextView) view.findViewById(R.id.conversation_list_date);
        mutedView = (ImageView) view.findViewById(R.id.conversation_list_muted);
        unreadView = (ImageView) view.findViewById(R.id.conversation_list_unread);
        errorIndicator = (ImageView) view.findViewById(R.id.conversation_list_error);
        mAvatarView = (AvatarView) view.findViewById(R.id.conversation_list_avatar);
        mSelected = (ImageView) view.findViewById(R.id.selected);
    }

    @Override
    public void onUpdate(final Contact updated) {
        boolean shouldUpdate = true;
        final Drawable drawable;
        final String name;

        int recipientCount = mData.getRecipients().size();

        if (recipientCount == 1) {
            Contact contact = mData.getRecipients().get(0);
            if (contact.getNumber().equals(updated.getNumber())) {
                drawable = contact.getAvatar(mContext, null);
                name = contact.getName();

                if (contact.existsInDatabase()) {
                    mAvatarView.assignContactUri(contact.getUri());
                } else {
                    mAvatarView.assignContactFromPhone(contact.getNumber(), true);
                }
            } else {
                // onUpdate was called because *some* contact was loaded, but it wasn't the contact for this
                // conversation, and thus we shouldn't update the UI because we won't be able to set the correct data
                drawable = null;
                name = "";
                shouldUpdate = false;
            }
        } else if (recipientCount > 1) {
            int count = recipientCount < 4 ? recipientCount : 4;
            ArrayList<Drawable> drawables = new ArrayList<>(count);

            int left = 0, top = 0, width = 100, height = 100;
            for(int i = 0; i < recipientCount; i++) {
                if (i >= count) break;
                Contact contact = mData.getRecipients().get(i);
                Drawable d = contact.getAvatar(mContext, null);
                if(d != null){
                    drawables.add(d);
                    height = d.getIntrinsicHeight() > height ? d.getIntrinsicHeight() : height;
                    width = d.getIntrinsicWidth() > width ? d.getIntrinsicWidth() : width;
                }
            }

            Bitmap bitmap = null;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            count = drawables.size();

            for(int i = 0; i < count; i++){
                if(i == 1)left += width/2;
                if(i == 2)top += height/2;
                if(i == 3)left -= width/2;

                c.drawBitmap(((BitmapDrawable)drawables.get(i)).getBitmap(), null, new Rect(left, top, width, height), null);
            }

            drawable = new BitmapDrawable(mContext.getResources(),bitmap);
            name = "" + mData.getRecipients().size();
            mAvatarView.assignContactUri(null);
        } else {
            drawable = null;
            name = "#";
            mAvatarView.assignContactUri(null);
        }

        final ConversationLegacy conversationLegacy = new ConversationLegacy(mContext, mData.getThreadId());

        if (shouldUpdate) {
            mContext.runOnUiThread(() -> {
                mAvatarView.setImageDrawable(drawable);
                mAvatarView.setContactName(name);
                fromView.setText(formatMessage(mData, conversationLegacy));
            });
        }
    }

    private CharSequence formatMessage(Conversation conversation, ConversationLegacy conversationLegacy) {
        String from = conversation.getRecipients().formatNames(", ");

        SpannableStringBuilder buf = new SpannableStringBuilder(from);

        if (conversation.getMessageCount() > 1 && mPrefs.getBoolean(SettingsFragment.MESSAGE_COUNT, false)) {
            int before = buf.length();
            buf.append(mContext.getResources().getString(R.string.message_count_format, conversation.getMessageCount()));
            buf.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.grey_light)), before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        if (conversationLegacy.hasDraft()) {
            buf.append(mContext.getResources().getString(R.string.draft_separator));
            int before = buf.length();
            buf.append(mContext.getResources().getString(R.string.has_draft));
            buf.setSpan(new ForegroundColorSpan(ThemeManager.getColor()), before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return buf;
    }
}
