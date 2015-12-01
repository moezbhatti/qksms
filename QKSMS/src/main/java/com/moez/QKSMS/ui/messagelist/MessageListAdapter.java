package com.moez.QKSMS.ui.messagelist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.provider.Telephony.TextBasedSmsColumns;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.mms.transaction.Transaction;
import com.android.mms.transaction.TransactionBundle;
import com.android.mms.transaction.TransactionService;
import com.android.mms.util.DownloadManager;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.emoji.EmojiRegistry;
import com.moez.QKSMS.common.utils.CursorUtils;
import com.moez.QKSMS.common.utils.MessageUtils;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.mms.MmsThumbnailPresenter;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.AvatarView;
import ezvcard.Ezvcard;
import ezvcard.VCard;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListAdapter extends RecyclerCursorAdapter<MessageListViewHolder, MessageItem> {
    private final String TAG = "MessageListAdapter";

    public static final int INCOMING_ITEM_TYPE_SMS = 0;
    public static final int OUTGOING_ITEM_TYPE_SMS = 1;
    public static final int INCOMING_ITEM_TYPE_MMS = 2;
    public static final int OUTGOING_ITEM_TYPE_MMS = 3;

    private ArrayList<Long> mSelectedConversations = new ArrayList<>();

    private MessageItemCache mMessageItemCache;
    private MessageColumns.ColumnsMap mColumnsMap;

    private final Resources mRes;
    private final SharedPreferences mPrefs;

    // Configuration options.
    private long mThreadId = -1;
    private long mRowId = -1;
    private Pattern mSearchHighlighter = null;
    private boolean mIsGroupConversation = false;
    private Handler mMessageListItemHandler = null; // TODO this isn't quite the same as the others
    private String mSelection = null;

    public MessageListAdapter(QKActivity context) {
        super(context);
        mRes = mContext.getResources();
        mPrefs = mContext.getPrefs();
    }

    protected MessageItem getItem(int position) {
        mCursor.moveToPosition(position);

        String type = mCursor.getString(mColumnsMap.mColumnMsgType);
        long msgId = mCursor.getLong(mColumnsMap.mColumnMsgId);

        return mMessageItemCache.get(type, msgId, mCursor);
    }

    public Cursor getCursorForItem(MessageItem item) {
        if (CursorUtils.isValid(mCursor) && mCursor.moveToFirst()) {
            do {
                long id = mCursor.getLong(mColumnsMap.mColumnMsgId);
                String type = mCursor.getString(mColumnsMap.mColumnMsgType);

                if (id == item.mMsgId && type != null && type.equals(item.mType)) {
                    return mCursor;
                }
            } while (mCursor.moveToNext());
        }
        return null;
    }

    public MessageColumns.ColumnsMap getColumnsMap() {
        return mColumnsMap;
    }

    public void setIsGroupConversation(boolean b) {
        mIsGroupConversation = b;
    }

    @Override
    public MessageListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int resource;
        boolean sent;

        if (viewType == INCOMING_ITEM_TYPE_SMS || viewType == INCOMING_ITEM_TYPE_MMS) {
            resource = R.layout.list_item_message_in;
            sent = false;
        } else {
            resource = R.layout.list_item_message_out;
            sent = true;
        }

        View view = inflater.inflate(resource, parent, false);
        return setupViewHolder(view, sent);
    }

    private MessageListViewHolder setupViewHolder(View view, boolean sent) {
        MessageListViewHolder holder = new MessageListViewHolder(mContext, view);

        if (sent) {
            // set up colors
            holder.mBodyTextView.setOnColorBackground(ThemeManager.getSentBubbleColor() == ThemeManager.getColor());
            holder.mDateView.setOnColorBackground(false);
            holder.mDeliveredIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.MULTIPLY);
            holder.mLockedIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.MULTIPLY);

            // set up avatar
            holder.mAvatarView.setImageDrawable(Contact.getMe(true).getAvatar(mContext, null));
            holder.mAvatarView.setContactName(AvatarView.ME);
            holder.mAvatarView.assignContactUri(ContactsContract.Profile.CONTENT_URI);
            if (mPrefs.getBoolean(SettingsFragment.HIDE_AVATAR_SENT, true)) {
                ((RelativeLayout.LayoutParams) holder.mMessageBlock.getLayoutParams()).setMargins(0, 0, 0, 0);
                holder.mAvatarView.setVisibility(View.GONE);
            }
        } else {
            // set up colors
            holder.mBodyTextView.setOnColorBackground(ThemeManager.getReceivedBubbleColor() == ThemeManager.getColor());
            holder.mDateView.setOnColorBackground(false);
            holder.mDeliveredIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.MULTIPLY);
            holder.mLockedIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.MULTIPLY);

            // set up avatar
            if (mPrefs.getBoolean(SettingsFragment.HIDE_AVATAR_RECEIVED, false)) {
                ((RelativeLayout.LayoutParams) holder.mMessageBlock.getLayoutParams()).setMargins(0, 0, 0, 0);
                holder.mAvatarView.setVisibility(View.GONE);
            }
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(MessageListViewHolder holder, int position) {
        MessageItem messageItem = getItem(position);

        holder.mData = messageItem;
        holder.mContext = mContext;
        holder.mClickListener = mItemClickListener;
        holder.mRoot.setOnClickListener(holder);
        holder.mRoot.setOnLongClickListener(holder);
        holder.mPresenter = null;

        // Here we're avoiding reseting the avatar to the empty avatar when we're rebinding
        // to the same item. This happens when there's a DB change which causes the message item
        // cache in the MessageListAdapter to getConversation cleared. When an mms MessageItem is newly
        // created, it has no info in it except the message id. The info is eventually loaded
        // and bindCommonMessage is called again (see onPduLoaded below). When we haven't loaded
        // the pdu, we don't want to call updateContactView because it
        // will set the avatar to the generic avatar then when this method is called again
        // from onPduLoaded, it will reset to the real avatar. This test is to avoid that flash.
        boolean pduLoaded = messageItem.isSms() || messageItem.mSlideshow != null;

        bindGrouping(holder, messageItem);
        bindBody(holder, messageItem);
        bindTimestamp(holder, messageItem);

        if (pduLoaded) {
            bindAvatar(holder, messageItem);
        }
        bindMmsView(holder, messageItem);
        bindIndicators(holder, messageItem);
        bindVcard(holder, messageItem);

        if (messageItem.mMessageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            bindNotifInd(holder, messageItem);
        } else {
            if (holder.mDownloadButton != null) {
                holder.mDownloadButton.setVisibility(View.GONE);
                holder.mDownloadingLabel.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Binds a MessageItem that hasn't been downloaded yet
     */
    private void bindNotifInd(final MessageListViewHolder holder, final MessageItem messageItem) {
        holder.showMmsView(false);

        switch (messageItem.getMmsDownloadStatus()) {
            case DownloadManager.STATE_PRE_DOWNLOADING:
            case DownloadManager.STATE_DOWNLOADING:
                showDownloadingAttachment(holder);
                break;
            case DownloadManager.STATE_UNKNOWN:
            case DownloadManager.STATE_UNSTARTED:
                DownloadManager downloadManager = DownloadManager.getInstance();
                boolean autoDownload = downloadManager.isAuto();
                boolean dataSuspended = (QKSMSApp.getApplication().getTelephonyManager()
                        .getDataState() == TelephonyManager.DATA_SUSPENDED);

                // If we're going to automatically start downloading the mms attachment, then
                // don't bother showing the download button for an instant before the actual
                // download begins. Instead, show downloading as taking place.
                if (autoDownload && !dataSuspended) {
                    showDownloadingAttachment(holder);
                    break;
                }
            case DownloadManager.STATE_TRANSIENT_FAILURE:
            case DownloadManager.STATE_PERMANENT_FAILURE:
            case DownloadManager.STATE_SKIP_RETRYING:
            default:
                holder.inflateDownloadControls();
                holder.mDownloadingLabel.setVisibility(View.GONE);
                holder.mDownloadButton.setVisibility(View.VISIBLE);
                holder.mDownloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.mDownloadingLabel.setVisibility(View.VISIBLE);
                        holder.mDownloadButton.setVisibility(View.GONE);
                        Intent intent = new Intent(mContext, TransactionService.class);
                        intent.putExtra(TransactionBundle.URI, messageItem.mMessageUri.toString());
                        intent.putExtra(TransactionBundle.TRANSACTION_TYPE, Transaction.RETRIEVE_TRANSACTION);
                        mContext.startService(intent);

                        DownloadManager.getInstance().markState(messageItem.mMessageUri, DownloadManager.STATE_PRE_DOWNLOADING);
                    }
                });
                break;
        }

        // Hide the indicators.
        holder.mLockedIndicator.setVisibility(View.GONE);
        holder.mDeliveredIndicator.setVisibility(View.GONE);
        holder.mDetailsIndicator.setVisibility(View.GONE);
    }

    private void showDownloadingAttachment(MessageListViewHolder holder) {
        holder.inflateDownloadControls();
        holder.mDownloadingLabel.setVisibility(View.VISIBLE);
        holder.mDownloadButton.setVisibility(View.GONE);
    }

    private void bindGrouping(MessageListViewHolder holder, MessageItem messageItem) {
        boolean showAvatar;
        boolean showTimestamp;

        int position = mCursor.getPosition();

        if (position == mCursor.getCount() - 1) {
            showTimestamp = true;
        } else if (messageItem.mDeliveryStatus != MessageItem.DeliveryStatus.NONE) {
            showTimestamp = true;
        } else if (messageItem.isFailedMessage()) {
            showTimestamp = true;
        } else if (messageItem.isSending()) {
            showTimestamp = true;
        } else {
            int MAX_DURATION = 60 * 60 * 1000;
            MessageItem messageItem2 = getItem(position + 1);
            showTimestamp = messageItem2.mDate - messageItem.mDate >= MAX_DURATION;
        }

        if (position == 0) {
            showAvatar = true;
        } else {
            int MAX_DURATION = 60 * 60 * 1000;
            MessageItem messageItem2 = getItem(position - 1);
            showAvatar = messageItem.getBoxId() != messageItem2.getBoxId() || messageItem.mDate - messageItem2.mDate >= MAX_DURATION;

            // If the messages are from different people, then we don't care about any of the other checks,
            // we need to show the avatar/timestamp
            if (messageItem.mAddress != null && messageItem2.mAddress != null && !messageItem.mAddress.equals(messageItem2.mAddress)) {
                showAvatar = true;
                showTimestamp = true;
            }
        }

        holder.mDateView.setVisibility(showTimestamp ? View.VISIBLE : View.GONE);
        holder.mSpace.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        holder.mBodyTextView.setBackgroundResource(showAvatar ? (messageItem.isMe() ? ThemeManager.getSentBubbleRes() :
                ThemeManager.getReceivedBubbleRes()) : (messageItem.isMe() ?
                ThemeManager.getSentBubbleAltRes() : ThemeManager.getReceivedBubbleAltRes()));

        if (messageItem.isMe()) {
            holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getSentBubbleColor(), PorterDuff.Mode.MULTIPLY);
        } else {
            holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getReceivedBubbleColor(), PorterDuff.Mode.MULTIPLY);
        }

        if (messageItem.isMe() && !mPrefs.getBoolean(SettingsFragment.HIDE_AVATAR_SENT, true)) {
            holder.mAvatarView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        } else if (!messageItem.isMe() && !mPrefs.getBoolean(SettingsFragment.HIDE_AVATAR_RECEIVED, false)) {
            holder.mAvatarView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        }
    }

    private void bindBody(MessageListViewHolder holder, MessageItem messageItem) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

        String body = messageItem.mBody;

        if (messageItem.mMessageType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            String msgSizeText = mContext.getString(R.string.message_size_label)
                    + String.valueOf((messageItem.mMessageSize + 1023) / 1024)
                    + mContext.getString(R.string.kilobyte);

            body = msgSizeText;
        }

        // Cleanse the subject
        String subject = MessageUtils.cleanseMmsSubject(mContext, messageItem.mSubject, body);
        boolean hasSubject = !TextUtils.isEmpty(subject);
        if (hasSubject) {
            buf.append(mContext.getResources().getString(R.string.inline_subject, subject));
        }

        if (!TextUtils.isEmpty(body)) {
            if (mPrefs.getBoolean(SettingsFragment.AUTO_EMOJI, false)) {
                body = EmojiRegistry.parseEmojis(body);
            }

            buf.append(body);
        }

        if (messageItem.mHighlight != null) {
            Matcher m = messageItem.mHighlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), 0);
            }
        }

        holder.mBodyTextView.setText(buf);
        holder.mBodyTextView.setVisibility(TextUtils.isEmpty(buf) ? View.GONE : View.VISIBLE);
    }

    private void bindTimestamp(MessageListViewHolder holder, MessageItem messageItem) {
        String timestamp;


        if (messageItem.isSending()) {
            timestamp = mContext.getString(R.string.status_sending);
        } else if (messageItem.mTimestamp != null && !messageItem.mTimestamp.equals("")) {
            timestamp = messageItem.mTimestamp;
        } else if (messageItem.isOutgoingMessage() && messageItem.isFailedMessage()) {
            timestamp = mContext.getResources().getString(R.string.status_failed);
        } else if (messageItem.isMms()) {
            timestamp = mContext.getString(R.string.loading);
        } else {
            timestamp = "";
        }

        if (!mIsGroupConversation || messageItem.isMe() || TextUtils.isEmpty(messageItem.mContact)) {
            holder.mDateView.setText(timestamp);
        } else {
            holder.mDateView.setText(mContext.getString(R.string.message_timestamp_format, timestamp, messageItem.mContact));
        }

    }

    private void bindAvatar(MessageListViewHolder holder, MessageItem messageItem) {
        if (!messageItem.isMe()) {
            Contact contact = Contact.get(messageItem.mAddress, true);
            holder.mAvatarView.setImageDrawable(contact.getAvatar(mContext, null));
            holder.mAvatarView.setContactName(contact.getName());
            if (contact.existsInDatabase()) {
                holder.mAvatarView.assignContactUri(contact.getUri());
            } else {
                holder.mAvatarView.assignContactFromPhone(contact.getNumber(), true);
            }
        }
    }

    private void bindMmsView(final MessageListViewHolder holder, MessageItem messageItem) {
        if (messageItem.isSms()) {
            holder.showMmsView(false);
            messageItem.setOnPduLoaded(null);
        } else {
            if (messageItem.mAttachmentType != SmsHelper.TEXT) {
                if (holder.mImageView == null) {
                    holder.setImage(null, null);
                }
                setImageViewOnClickListener(holder, messageItem);
                drawPlaybackButton(holder, messageItem);
            } else {
                holder.showMmsView(false);
            }

            if (messageItem.mSlideshow == null) {
                messageItem.setOnPduLoaded(messageItem1 -> {
                    if (messageItem1 != null && messageItem1.getMessageId() == messageItem1.getMessageId()) {
                        messageItem1.setCachedFormattedMessage(null);
                        bindGrouping(holder, messageItem);
                        bindBody(holder, messageItem);
                        bindTimestamp(holder, messageItem);
                        bindAvatar(holder, messageItem);
                        bindMmsView(holder, messageItem);
                        bindIndicators(holder, messageItem);
                        bindVcard(holder, messageItem);
                    }
                });
            } else {
                if (holder.mPresenter == null) {
                    holder.mPresenter = new MmsThumbnailPresenter(mContext, holder, messageItem.mSlideshow);
                } else {
                    holder.mPresenter.setModel(messageItem.mSlideshow);
                    holder.mPresenter.setView(holder);
                }
                if (holder.mImageLoadedCallback == null) {
                    holder.mImageLoadedCallback = new MessageListViewHolder.ImageLoadedCallback(holder);
                } else {
                    holder.mImageLoadedCallback.reset(holder);
                }
                holder.mPresenter.present(holder.mImageLoadedCallback);
            }
        }
    }

    private void bindIndicators(MessageListViewHolder holder, MessageItem messageItem) {
        // Locked icon
        if (messageItem.mLocked) {
            holder.mLockedIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mLockedIndicator.setVisibility(View.GONE);
        }

        // Delivery icon - we can show a failed icon for both sms and mms, but for an actual
        // delivery, we only show the icon for sms. We don't have the information here in mms to
        // know whether the message has been delivered. For mms, msgItem.mDeliveryStatus set
        // to MessageItem.DeliveryStatus.RECEIVED simply means the setting requesting a
        // delivery report was turned on when the message was sent. Yes, it's confusing!
        if ((messageItem.isOutgoingMessage() && messageItem.isFailedMessage()) ||
                messageItem.mDeliveryStatus == MessageItem.DeliveryStatus.FAILED) {
            holder.mDeliveredIndicator.setVisibility(View.VISIBLE);
        } else if (messageItem.isSms() &&
                messageItem.mDeliveryStatus == MessageItem.DeliveryStatus.RECEIVED) {
            holder.mDeliveredIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mDeliveredIndicator.setVisibility(View.GONE);
        }

        // Message details icon - this icon is shown both for sms and mms messages. For mms,
        // we show the icon if the read report or delivery report setting was set when the
        // message was sent. Showing the icon tells the user there's more information
        // by selecting the "View report" menu.
        if (messageItem.mDeliveryStatus == MessageItem.DeliveryStatus.INFO || messageItem.mReadReport
                || (messageItem.isMms() && messageItem.mDeliveryStatus == MessageItem.DeliveryStatus.RECEIVED)) {
            holder.mDetailsIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mDetailsIndicator.setVisibility(View.GONE);
        }
    }

    private void bindVcard(MessageListViewHolder holder, MessageItem messageItem) {
        if (!ContentType.TEXT_VCARD.equals(messageItem.mTextContentType)) {
            return;
        }

        VCard vCard = Ezvcard.parse(messageItem.mBody).first();

        SpannableString name = new SpannableString(vCard.getFormattedName().getValue());
        name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
        holder.mBodyTextView.setText(name);
    }

    private void setImageViewOnClickListener(MessageListViewHolder holder, final MessageItem msgItem) {
        if (holder.mImageView != null) {
            switch (msgItem.mAttachmentType) {
                case SmsHelper.IMAGE:
                case SmsHelper.VIDEO:
                    holder.mImageView.setOnClickListener(holder);
                    holder.mImageView.setOnLongClickListener(holder);
                    break;

                default:
                    holder.mImageView.setOnClickListener(null);
                    break;
            }
        }
    }

    private void drawPlaybackButton(MessageListViewHolder holder, MessageItem msgItem) {
        if (holder.mSlideShowButton != null) {
            switch (msgItem.mAttachmentType) {
                case SmsHelper.SLIDESHOW:
                case SmsHelper.AUDIO:
                case SmsHelper.VIDEO:
                    // Show the 'Play' button and bind message info on it.
                    holder.mSlideShowButton.setTag(msgItem);
                    // Set call-back for the 'Play' button.
                    holder.mSlideShowButton.setOnClickListener(holder);
                    holder.mSlideShowButton.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.mSlideShowButton.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);

        if (CursorUtils.isValid(cursor)) {
            mColumnsMap = new MessageColumns.ColumnsMap(cursor);
            mMessageItemCache = new MessageItemCache(mContext, mColumnsMap, mSearchHighlighter, MessageColumns.CACHE_SIZE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // This method shouldn't be called if our cursor is null, since the framework should know
        // that there aren't any items to look at in that case
        MessageItem item = getItem(position);
        int boxId = item.getBoxId();

        if (item.isSms()) {
            if (boxId == TextBasedSmsColumns.MESSAGE_TYPE_INBOX || boxId == TextBasedSmsColumns.MESSAGE_TYPE_ALL) {
                return INCOMING_ITEM_TYPE_SMS;
            } else {
                return OUTGOING_ITEM_TYPE_SMS;
            }
        } else {
            if (boxId == Telephony.Mms.MESSAGE_BOX_ALL || boxId == Telephony.Mms.MESSAGE_BOX_INBOX) {
                return INCOMING_ITEM_TYPE_MMS;
            } else {
                return OUTGOING_ITEM_TYPE_MMS;
            }
        }
    }

}
