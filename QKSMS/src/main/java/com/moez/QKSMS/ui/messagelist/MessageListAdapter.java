package com.moez.QKSMS.ui.messagelist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.koushikdutta.ion.Ion;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.common.QKPreferences;
import com.moez.QKSMS.common.SmsHelper;
import com.moez.QKSMS.common.ThemeManager;
import com.moez.QKSMS.common.emoji.EmojiRegistry;
import com.moez.QKSMS.common.utils.CursorUtils;
import com.moez.QKSMS.common.utils.LinkifyUtils;
import com.moez.QKSMS.common.utils.MessageUtils;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;
import com.moez.QKSMS.ui.mms.MmsThumbnailPresenter;
import com.moez.QKSMS.ui.view.AvatarView;
import ezvcard.Ezvcard;
import ezvcard.VCard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListAdapter extends RecyclerCursorAdapter<MessageListViewHolder, MessageItem> {
    private final String TAG = "MessageListAdapter";

    public static final int INCOMING_ITEM = 0;
    public static final int OUTGOING_ITEM = 1;


    private static final Pattern urlPattern = Pattern.compile(
            "\\b(https?:\\/\\/\\S+(?:png|jpe?g|gif)\\S*)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private MessageItemCache mMessageItemCache;
    private MessageColumns.ColumnsMap mColumnsMap;

    private final Resources mRes;
    private final SharedPreferences mPrefs;

    // Configuration options.
    private Pattern mSearchHighlighter = null;
    private boolean mIsGroupConversation = false;

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

        if (viewType == INCOMING_ITEM) {
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
            holder.mBodyTextView.setOnColorBackground(ThemeManager.getSentBubbleColor() != ThemeManager.getNeutralBubbleColor());
            holder.mDateView.setOnColorBackground(false);
            holder.mDeliveredIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);
            holder.mLockedIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);

            // set up avatar
            holder.mAvatarView.setImageDrawable(Contact.getMe(true).getAvatar(mContext, null));
            holder.mAvatarView.setContactName(AvatarView.ME);
            holder.mAvatarView.assignContactUri(ContactsContract.Profile.CONTENT_URI);
            if (QKPreferences.getBoolean(QKPreference.HIDE_AVATAR_SENT)) {
                ((RelativeLayout.LayoutParams) holder.mMessageBlock.getLayoutParams()).setMargins(0, 0, 0, 0);
                holder.mAvatarView.setVisibility(View.GONE);
            }
        } else {
            // set up colors
            holder.mBodyTextView.setOnColorBackground(ThemeManager.getReceivedBubbleColor() != ThemeManager.getNeutralBubbleColor());
            holder.mDateView.setOnColorBackground(false);
            holder.mDeliveredIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);
            holder.mLockedIndicator.setColorFilter(ThemeManager.getTextOnBackgroundSecondary(), PorterDuff.Mode.SRC_ATOP);

            // set up avatar
            if (QKPreferences.getBoolean(QKPreference.HIDE_AVATAR_RECEIVED)) {
                ((RelativeLayout.LayoutParams) holder.mMessageBlock.getLayoutParams()).setMargins(0, 0, 0, 0);
                holder.mAvatarView.setVisibility(View.GONE);
            }
        }

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            holder.mRoot.setBackgroundDrawable(ThemeManager.getRippleBackground());
            holder.mSlideShowButton.setBackgroundDrawable(ThemeManager.getRippleBackground());
            holder.mMmsView.getForeground().setColorFilter(ThemeManager.getBackgroundColor(), PorterDuff.Mode.SRC_ATOP);
        });

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
        // cache in the MessageListAdapter to get cleared. When an mms MessageItem is newly
        // created, it has no info in it except the message id. The info is eventually loaded
        // and bindCommonMessage is called again (see onPduLoaded below). When we haven't loaded
        // the pdu, we don't want to call updateContactView because it
        // will set the avatar to the generic avatar then when this method is called again
        // from onPduLoaded, it will reset to the real avatar. This test is to avoid that flash.
        boolean pduLoaded = messageItem.isSms() || messageItem.mSlideshow != null;

        bindGrouping(holder, messageItem);
        bindTimestamp(holder, messageItem);

        if (pduLoaded) {
            bindAvatar(holder, messageItem);
        }
        bindMmsView(holder, messageItem);
        bindBody(holder, messageItem);
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
                holder.mDownloadButton.setOnClickListener(v -> {
                    holder.mDownloadingLabel.setVisibility(View.VISIBLE);
                    holder.mDownloadButton.setVisibility(View.GONE);
                    Intent intent = new Intent(mContext, TransactionService.class);
                    intent.putExtra(TransactionBundle.URI, messageItem.mMessageUri.toString());
                    intent.putExtra(TransactionBundle.TRANSACTION_TYPE, Transaction.RETRIEVE_TRANSACTION);
                    mContext.startService(intent);

                    DownloadManager.getInstance().markState(messageItem.mMessageUri, DownloadManager.STATE_PRE_DOWNLOADING);
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

    private boolean shouldShowTimestamp(MessageItem messageItem, int position) {
        if (position == mCursor.getCount() - 1) {
            return true;
        }

        MessageItem messageItem2 = getItem(position + 1);

        if (QKPreferences.getBoolean(QKPreference.FORCE_TIMESTAMPS)) {
            return true;
        } else if (messageItem.mDeliveryStatus != MessageItem.DeliveryStatus.NONE) {
            return true;
        } else if (messageItem.isFailedMessage()) {
            return true;
        } else if (messageItem.isSending()) {
            return true;
        } else if (messagesFromDifferentPeople(messageItem, messageItem2)) {
            return true;
        } else {
            int MAX_DURATION = Integer.parseInt(QKPreferences.getString(QKPreference.NEW_TIMESTAMP_DELAY)) * 60 * 1000;
            return (messageItem2.mDate - messageItem.mDate >= MAX_DURATION);
        }
    }

    private boolean shouldShowAvatar(MessageItem messageItem, int position) {
        if (position == 0) {
            return true;
        }

        MessageItem messageItem2 = getItem(position - 1);

        if (messagesFromDifferentPeople(messageItem, messageItem2)) {
            // If the messages are from different people, then we don't care about any of the other checks,
            // we need to show the avatar/timestamp. This is used for group chats, which is why we want
            // both to be incoming messages
            return true;
        } else {
            int MAX_DURATION = 60 * 60 * 1000;
            return (messageItem.getBoxId() != messageItem2.getBoxId() || messageItem.mDate - messageItem2.mDate >= MAX_DURATION);
        }
    }

    private boolean messagesFromDifferentPeople(MessageItem a, MessageItem b) {
        return (a.mAddress != null && b.mAddress != null &&
                !a.mAddress.equals(b.mAddress) &&
                !a.isOutgoingMessage(

                ) && !b.isOutgoingMessage());
    }

    private int getBubbleBackgroundResource(boolean showAvatar, boolean isMine) {
        if (showAvatar && isMine) return ThemeManager.getSentBubbleRes();
        else if (showAvatar && !isMine) return ThemeManager.getReceivedBubbleRes();
        else if (!showAvatar && isMine) return ThemeManager.getSentBubbleAltRes();
        else if (!showAvatar && !isMine) return ThemeManager.getReceivedBubbleAltRes();
        else return -1;
    }

    private void bindGrouping(MessageListViewHolder holder, MessageItem messageItem) {
        int position = mCursor.getPosition();

        boolean showAvatar = shouldShowAvatar(messageItem, position);
        boolean showTimestamp = shouldShowTimestamp(messageItem, position);

        holder.mDateView.setVisibility(showTimestamp ? View.VISIBLE : View.GONE);
        holder.mSpace.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        holder.mBodyTextView.setBackgroundResource(getBubbleBackgroundResource(showAvatar, messageItem.isMe()));

        holder.setLiveViewCallback(key -> {
            if (messageItem.isMe()) {
                holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getSentBubbleColor(), PorterDuff.Mode.SRC_ATOP);
            } else {
                holder.mBodyTextView.getBackground().setColorFilter(ThemeManager.getReceivedBubbleColor(), PorterDuff.Mode.SRC_ATOP);
            }
        });

        if (messageItem.isMe() && !QKPreferences.getBoolean(QKPreference.HIDE_AVATAR_SENT)) {
            holder.mAvatarView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        } else if (!messageItem.isMe() && !QKPreferences.getBoolean(QKPreference.HIDE_AVATAR_RECEIVED)) {
            holder.mAvatarView.setVisibility(showAvatar ? View.VISIBLE : View.GONE);
        }
    }

    private void bindBody(MessageListViewHolder holder, MessageItem messageItem) {
        holder.mBodyTextView.setAutoLinkMask(0);
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
            if (QKPreferences.getBoolean(QKPreference.AUTO_EMOJI)) {
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

        if (!TextUtils.isEmpty(buf)) {
            holder.mBodyTextView.setText(buf);
            Matcher matcher = urlPattern.matcher(holder.mBodyTextView.getText());
            if (matcher.find()) { //only find the image to the first link
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                String imageUrl = buf.subSequence(matchStart, matchEnd).toString();
                Ion.with(mContext).load(imageUrl).withBitmap().asBitmap().setCallback((e, result) -> {
                    try {
                        holder.setImage("url_img" + holder.getItemId(), result);
                        holder.mImageView.setOnClickListener(v -> {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                            mContext.startActivity(i);
                        });
                    } catch (NullPointerException imageException) {
                        imageException.printStackTrace();
                    }
                });
            }
            LinkifyUtils.addLinks(holder.mBodyTextView);
        }
        holder.mBodyTextView.setVisibility(TextUtils.isEmpty(buf) ? View.GONE : View.VISIBLE);
        holder.mBodyTextView.setOnClickListener(v -> holder.mRoot.callOnClick());
        holder.mBodyTextView.setOnLongClickListener(v -> holder.mRoot.performLongClick());
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
                    if (mCursor == null) {
                        // The pdu has probably loaded after shutting down the fragment. Don't try to bind anything now
                        return;
                    }
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
        if (CursorUtils.isValid(cursor)) {
            mColumnsMap = new MessageColumns.ColumnsMap(cursor);
            mMessageItemCache = new MessageItemCache(mContext, mColumnsMap, mSearchHighlighter, MessageColumns.CACHE_SIZE);
        }

        super.changeCursor(cursor);
    }

    @Override
    public int getItemViewType(int position) {
        // This method shouldn't be called if our cursor is null, since the framework should know
        // that there aren't any items to look at in that case
        MessageItem item = getItem(position);
        int boxId = item.getBoxId();

        if (item.isSms()) {
            if (boxId == TextBasedSmsColumns.MESSAGE_TYPE_INBOX || boxId == TextBasedSmsColumns.MESSAGE_TYPE_ALL) {
                return INCOMING_ITEM;
            } else {
                return OUTGOING_ITEM;
            }
        } else {
            if (boxId == Telephony.Mms.MESSAGE_BOX_ALL || boxId == Telephony.Mms.MESSAGE_BOX_INBOX) {
                return INCOMING_ITEM;
            } else {
                return OUTGOING_ITEM;
            }
        }
    }

}
