/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moez.QKSMS.ui.messagelist;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.Log;

import com.android.mms.util.DownloadManager;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.MultimediaMessagePdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.moez.QKSMS.QKSMSApp;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.formatter.FormatterFactory;
import com.moez.QKSMS.data.Contact;
import com.moez.QKSMS.model.SlideModel;
import com.moez.QKSMS.model.SlideshowModel;
import com.moez.QKSMS.model.TextModel;
import com.moez.QKSMS.common.google.ItemLoadedCallback;
import com.moez.QKSMS.common.google.ItemLoadedFuture;
import com.moez.QKSMS.common.google.PduLoaderManager;
import com.moez.QKSMS.common.utils.AddressUtils;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.transaction.SmsHelper;

import java.util.regex.Pattern;

/**
 * Mostly immutable model for an SMS/MMS message.
 *
 * <p>The only mutable field is the cached formatted message member,
 * the formatting of which is done outside this model in MessageListItem.
 */
public class MessageItem {
    private static String TAG = "MessageItem";

    public enum DeliveryStatus  { NONE, INFO, FAILED, PENDING, RECEIVED }

    public static int ATTACHMENT_TYPE_NOT_LOADED = -1;

    final Context mContext;
    public final String mType;
    public final long mMsgId;
    public final int mBoxId;

    public String mDeliveryStatusString;
    public DeliveryStatus mDeliveryStatus;
    public String mReadReportString;
    public boolean mReadReport;
    public boolean mLocked;            // locked to prevent auto-deletion

    public long mDate;
    public String mTimestamp;
    public String mAddress;
    public String mContact;
    public String mBody; // Body of SMS, first text of MMS.
    public String mTextContentType; // ContentType of text of MMS.
    public Pattern mHighlight; // portion of message to highlight (from search)

    // The only non-immutable field.  Not synchronized, as access will
    // only be from the main GUI thread.  Worst case if accessed from
    // another thread is it'll return null and be set again from that
    // thread.
    public CharSequence mCachedFormattedMessage;

    // The last message is cached above in mCachedFormattedMessage. In the latest design, we
    // show "Sending..." in place of the timestamp when a message is being sent. mLastSendingState
    // is used to keep track of the last sending state so that if the current sending state is
    // different, we can clear the message cache so it will getConversation rebuilt and recached.
    public boolean mLastSendingState;

    // Fields for MMS only.
    public Uri mMessageUri;
    public int mMessageType;
    public int mAttachmentType;
    public String mSubject;
    public SlideshowModel mSlideshow;
    public int mMessageSize;
    public int mErrorType;
    public int mErrorCode;
    public int mMmsStatus;
    public MessageColumns.ColumnsMap mColumnsMap;
    private PduLoadedCallback mPduLoadedCallback;
    private ItemLoadedFuture mItemLoadedFuture;

    @SuppressLint("NewApi")
    public MessageItem(Context context, String type, final Cursor cursor,
            final MessageColumns.ColumnsMap columnsMap, Pattern highlight,
            boolean canBlock) throws MmsException {
        mContext = context;
        mMsgId = cursor.getLong(columnsMap.mColumnMsgId);
        mHighlight = highlight;
        mType = type;
        mColumnsMap = columnsMap;

        if ("sms".equals(type)) {
            mReadReport = false; // No read reports in sms

            long status = cursor.getLong(columnsMap.mColumnSmsStatus);
            if (status == Sms.STATUS_NONE) {
                // No delivery report requested
                mDeliveryStatus = DeliveryStatus.NONE;
            } else if (status >= Sms.STATUS_FAILED) {
                // Failure
                mDeliveryStatus = DeliveryStatus.FAILED;
            } else if (status >= Sms.STATUS_PENDING) {
                // Pending
                mDeliveryStatus = DeliveryStatus.PENDING;
            } else {
                // Success
                mDeliveryStatus = DeliveryStatus.RECEIVED;
            }

            mMessageUri = ContentUris.withAppendedId(Sms.CONTENT_URI, mMsgId);
            // Set contact and message body
            mBoxId = cursor.getInt(columnsMap.mColumnSmsType);
            mAddress = cursor.getString(columnsMap.mColumnSmsAddress);
            if (SmsHelper.isOutgoingFolder(mBoxId)) {
                String meString = context.getString(
                        R.string.messagelist_sender_self);

                mContact = meString;
            } else {
                // For incoming messages, the ADDRESS field contains the sender.
                mContact = Contact.get(mAddress, canBlock).getName();
            }
            mBody = cursor.getString(columnsMap.mColumnSmsBody);
            mBody = FormatterFactory.format(mBody);

            // Unless the message is currently in the progress of being sent, it gets a time stamp.
            if (!isOutgoingMessage()) {
                // Set "received" or "sent" time stamp
                mDate = cursor.getLong(columnsMap.mColumnSmsDate);
                mTimestamp = DateFormatter.getMessageTimestamp(context, mDate);
            }

            mLocked = cursor.getInt(columnsMap.mColumnSmsLocked) != 0;
            mErrorCode = cursor.getInt(columnsMap.mColumnSmsErrorCode);
        } else if ("mms".equals(type)) {
            mMessageUri = ContentUris.withAppendedId(Mms.CONTENT_URI, mMsgId);
            mBoxId = cursor.getInt(columnsMap.mColumnMmsMessageBox);
            // If we can block, getConversation the address immediately from the "addr" table.
            if (canBlock) {
                mAddress = AddressUtils.getFrom(mContext, mMessageUri);
            }
            mMessageType = cursor.getInt(columnsMap.mColumnMmsMessageType);
            mErrorType = cursor.getInt(columnsMap.mColumnMmsErrorType);
            String subject = cursor.getString(columnsMap.mColumnMmsSubject);
            if (!TextUtils.isEmpty(subject)) {
                EncodedStringValue v = new EncodedStringValue(
                        cursor.getInt(columnsMap.mColumnMmsSubjectCharset),
                        PduPersister.getBytes(subject));
                mSubject = SmsHelper.cleanseMmsSubject(context, v.getString());
            }
            mLocked = cursor.getInt(columnsMap.mColumnMmsLocked) != 0;
            mSlideshow = null;
            mDeliveryStatusString = cursor.getString(columnsMap.mColumnMmsDeliveryReport);
            mReadReportString = cursor.getString(columnsMap.mColumnMmsReadReport);
            mBody = null;
            mMessageSize = 0;
            mTextContentType = null;
            // Initialize the time stamp to "" instead of null
            mTimestamp = "";
            mMmsStatus = cursor.getInt(columnsMap.mColumnMmsStatus);
            mAttachmentType = cursor.getInt(columnsMap.mColumnMmsTextOnly) != 0 ?
                    SmsHelper.TEXT : ATTACHMENT_TYPE_NOT_LOADED;

            // Start an async load of the pdu. If the pdu is already loaded, the callback
            // will getConversation called immediately
            boolean loadSlideshow = mMessageType != PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;

            mItemLoadedFuture = QKSMSApp.getApplication().getPduLoaderManager()
                    .getPdu(mMessageUri, loadSlideshow,
                    new PduLoadedMessageItemCallback());

        } else {
            throw new MmsException("Unknown type of the message: " + type);
        }
    }

    private DeliveryStatus getDeliveryStatus(String deliveryReport) {
        DeliveryStatus result;
        if (deliveryReport == null || !mAddress.equals(mContext.getString(
                R.string.messagelist_sender_self))) {
            result = DeliveryStatus.NONE;
        } else {
            int reportInt;
            try {
                reportInt = Integer.parseInt(deliveryReport);
                if (reportInt == PduHeaders.VALUE_YES) {
                    result = DeliveryStatus.RECEIVED;
                } else {
                    result = DeliveryStatus.NONE;
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Value for delivery report was invalid.");
                result = DeliveryStatus.NONE;
            }
        }
        return result;
    }

    private boolean getReadReport(String readReport) {
        boolean result;
        if (readReport == null || !mAddress.equals(mContext.getString(
                R.string.messagelist_sender_self))) {
            result = false;
        } else {
            int reportInt;
            try {
                reportInt = Integer.parseInt(readReport);
                result = (reportInt == PduHeaders.VALUE_YES);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Value for read report was invalid.");
                result = false;
            }
        }
        return result;
    }

    private void interpretFrom(EncodedStringValue from, Uri messageUri) {
        if (from != null) {
            mAddress = from.getString();
        } else {
            // In the rare case when getting the "from" address from the pdu fails,
            // (e.g. from == null) fall back to a slower, yet more reliable method of
            // getting the address from the "addr" table. This is what the Messaging
            // notification system uses.
            mAddress = AddressUtils.getFrom(mContext, messageUri);
        }
        mContact = TextUtils.isEmpty(mAddress) ? "" : Contact.get(mAddress, false).getName();
    }

    public boolean isMms() {
        return mType.equals("mms");
    }

    public boolean isSms() {
        return mType.equals("sms");
    }

    public boolean isDownloaded() {
        return (mMessageType != PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND);
    }

    public boolean isMe() {
        // Logic matches MessageListAdapter.getItemViewType which is used to decide which
        // type of MessageListItem to create: a left or right justified item depending on whether
        // the message is incoming or outgoing.
        boolean isIncomingMms = isMms()
                                    && (mBoxId == Mms.MESSAGE_BOX_INBOX
                                            || mBoxId == Mms.MESSAGE_BOX_ALL);
        boolean isIncomingSms = isSms()
                                    && (mBoxId == Sms.MESSAGE_TYPE_INBOX
                                            || mBoxId == Sms.MESSAGE_TYPE_ALL);
        return !(isIncomingMms || isIncomingSms);
    }

    public boolean isOutgoingMessage() {
        boolean isOutgoingMms = isMms() && (mBoxId == Mms.MESSAGE_BOX_OUTBOX);
        boolean isOutgoingSms = isSms()
                                    && ((mBoxId == Sms.MESSAGE_TYPE_FAILED)
                                            || (mBoxId == Sms.MESSAGE_TYPE_OUTBOX)
                                            || (mBoxId == Sms.MESSAGE_TYPE_QUEUED));
        return isOutgoingMms || isOutgoingSms;
    }

    public boolean isSending() {
        return !isFailedMessage() && isOutgoingMessage();
    }

    public boolean isFailedMessage() {
        boolean isFailedMms = isMms()
                            && (mErrorType >= MmsSms.ERR_TYPE_GENERIC_PERMANENT);
        boolean isFailedSms = isSms()
                            && (mBoxId == Sms.MESSAGE_TYPE_FAILED);
        return isFailedMms || isFailedSms;
    }

    // Note: This is the only mutable field in this class.  Think of
    // mCachedFormattedMessage as a C++ 'mutable' field on a const
    // object, with this being a lazy accessor whose logic to set it
    // is outside the class for model/view separation reasons.  In any
    // case, please keep this class conceptually immutable.
    public void setCachedFormattedMessage(CharSequence formattedMessage) {
        mCachedFormattedMessage = formattedMessage;
    }

    public CharSequence getCachedFormattedMessage() {
        boolean isSending = isSending();
        if (isSending != mLastSendingState) {
            mLastSendingState = isSending;
            mCachedFormattedMessage = null;         // clear cache so we'll rebuild the message
                                                    // to show "Sending..." or the sent date.
        }
        return mCachedFormattedMessage;
    }

    public int getBoxId() {
        return mBoxId;
    }

    public long getMessageId() {
        return mMsgId;
    }

    public int getMmsDownloadStatus() {
        return mMmsStatus & ~DownloadManager.DEFERRED_MASK;
    }

    @Override
    public String toString() {
        return "type: " + mType +
            " box: " + mBoxId +
            " uri: " + mMessageUri +
            " address: " + mAddress +
            " contact: " + mContact +
            " read: " + mReadReport +
            " delivery status: " + mDeliveryStatus;
    }

    public class PduLoadedMessageItemCallback implements ItemLoadedCallback {
        public void onItemLoaded(Object result, Throwable exception) {
            if (exception != null) {
                Log.e(TAG, "PduLoadedMessageItemCallback PDU couldn't be loaded: ", exception);
                return;
            }
            if (mItemLoadedFuture != null) {
                synchronized(mItemLoadedFuture) {
                    mItemLoadedFuture.setIsDone(true);
                }
            }
            PduLoaderManager.PduLoaded pduLoaded = (PduLoaderManager.PduLoaded)result;
            long timestamp = 0L;
            if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == mMessageType) {
                mDeliveryStatus = DeliveryStatus.NONE;
                NotificationInd notifInd = (NotificationInd)pduLoaded.mPdu;
                interpretFrom(notifInd.getFrom(), mMessageUri);
                // Borrow the mBody to hold the URL of the message.
                mBody = new String(notifInd.getContentLocation());
                mMessageSize = (int) notifInd.getMessageSize();
                timestamp = notifInd.getExpiry() * 1000L;
            } else {
                MultimediaMessagePdu msg = (MultimediaMessagePdu)pduLoaded.mPdu;
                mSlideshow = pduLoaded.mSlideshow;
                mAttachmentType = SmsHelper.getAttachmentType(mSlideshow, msg);

                if (mMessageType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) {
                    if (msg == null) {
                        interpretFrom(null, mMessageUri);
                    } else {
                        RetrieveConf retrieveConf = (RetrieveConf) msg;
                        interpretFrom(retrieveConf.getFrom(), mMessageUri);
                        timestamp = retrieveConf.getDate() * 1000L;
                    }
                } else {
                    // Use constant string for outgoing messages
                    mContact = mAddress =
                            mContext.getString(R.string.messagelist_sender_self);
                    timestamp = msg == null ? 0 : msg.getDate() * 1000L;
                }

                SlideModel slide = mSlideshow == null ? null : mSlideshow.get(0);
                if ((slide != null) && slide.hasText()) {
                    TextModel tm = slide.getText();
                    mBody = tm.getText();
                    mTextContentType = tm.getContentType();
                }

                mMessageSize = mSlideshow == null ? 0 : mSlideshow.getTotalMessageSize();

                mDeliveryStatus = getDeliveryStatus(mDeliveryStatusString);
                mReadReport = getReadReport(mReadReportString);
            }
            if (!isOutgoingMessage()) {
                if (PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND == mMessageType) {
                    mTimestamp = mContext.getString(R.string.expire_on, DateFormatter.getMessageTimestamp(mContext, timestamp));
                } else {
                    mTimestamp = DateFormatter.getMessageTimestamp(mContext, timestamp);
                }
            }
            if (mPduLoadedCallback != null) {
                mPduLoadedCallback.onPduLoaded(MessageItem.this);
            }
        }
    }

    public void setOnPduLoaded(PduLoadedCallback pduLoadedCallback) {
        mPduLoadedCallback = pduLoadedCallback;
    }

    public void cancelPduLoading() {
        if (mItemLoadedFuture != null && !mItemLoadedFuture.isDone()) {
            mItemLoadedFuture.cancel(mMessageUri);
            mItemLoadedFuture = null;
        }
    }

    public interface PduLoadedCallback {
        /**
         * Called when this item's pdu and slideshow are finished loading.
         *
         * @param messageItem the MessageItem that finished loading.
         */
        void onPduLoaded(MessageItem messageItem);
    }

    public SlideshowModel getSlideshow() {
        return mSlideshow;
    }
}
