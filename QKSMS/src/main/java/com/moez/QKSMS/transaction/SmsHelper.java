package com.moez.QKSMS.transaction;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.MultimediaMessagePdu;
import com.google.android.mms.pdu_alt.PduPersister;
import com.moez.QKSMS.MmsConfig;
import com.moez.QKSMS.R;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.data.Message;
import com.moez.QKSMS.mmssms.Settings;
import com.moez.QKSMS.model.SlideModel;
import com.moez.QKSMS.model.SlideshowModel;
import com.moez.QKSMS.ui.messagelist.MessageColumns;
import com.moez.QKSMS.ui.messagelist.MessageItem;
import com.moez.QKSMS.ui.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsHelper {

    public static final Uri SMS_CONTENT_PROVIDER = Uri.parse("content://sms/");
    public static final Uri MMS_CONTENT_PROVIDER = Uri.parse("content://mms/");
    public static final Uri SENT_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/sent");
    public static final Uri DRAFTS_CONTENT_PROVIDER = Uri.parse("content://sms/draft");
    public static final Uri PENDING_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/outbox");
    public static final Uri RECEIVED_MESSAGE_CONTENT_PROVIDER = Uri.parse("content://sms/inbox");
    public static final Uri CONVERSATIONS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations?simple=true");
    public static final Uri ADDRESSES_CONTENT_PROVIDER = Uri.parse("content://mms-sms/canonical-addresses");

    public static final String MAX_MMS_ATTACHMENT_SIZE_UNLIMITED = "unlimited";
    public static final String MAX_MMS_ATTACHMENT_SIZE_300KB = "300kb";
    public static final String MAX_MMS_ATTACHMENT_SIZE_600KB = "600kb";
    public static final String MAX_MMS_ATTACHMENT_SIZE_1MB = "1mb";

    public static final String sortDateDesc = "date DESC";
    public static final String sortDateAsc = "date ASC";

    public static final byte UNREAD = 0;
    public static final byte READ = 1;

    // Attachment types
    public static final int TEXT = 0;
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    public static final int AUDIO = 3;
    public static final int SLIDESHOW = 4;

    // Columns for SMS content providers
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_THREAD_ID = "thread_id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_RECIPIENT = "recipient_ids";
    public static final String COLUMN_PERSON = "person";
    public static final String COLUMN_SNIPPET = "snippet";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DATE_NORMALIZED = "normalized_date";
    public static final String COLUMN_DATE_SENT = "date_sent";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_ERROR = "error";
    public static final String COLUMN_READ = "read";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_MMS = "ct_t";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_SUB = "sub";
    public static final String COLUMN_MSG_BOX = "msg_box";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_SEEN = "seen";

    public static final String UNREAD_SELECTION = COLUMN_READ + " = " + UNREAD;
    public static final String UNSEEN_SELECTION = COLUMN_SEEN + " = " + UNREAD;
    public static final String FAILED_SELECTION = COLUMN_TYPE + " = " + Message.FAILED;

    public static final int ADDRESSES_ADDRESS = 1;

    private static final String TAG = "SMSHelper";
    private static SmsManager sms;

    private static Settings sendSettings;

    public SmsHelper() {

    }

    /**
     * The quality parameter which is used to compress JPEG images.
     */
    public static final int IMAGE_COMPRESSION_QUALITY = 95;
    /**
     * The minimum quality parameter which is used to compress JPEG images.
     */
    public static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;

    /**
     * Message type: all messages.
     */
    public static final int MESSAGE_TYPE_ALL = 0;

    /**
     * Message type: inbox.
     */
    public static final int MESSAGE_TYPE_INBOX = 1;

    /**
     * Message type: sent messages.
     */
    public static final int MESSAGE_TYPE_SENT = 2;

    /**
     * Message type: drafts.
     */
    public static final int MESSAGE_TYPE_DRAFT = 3;

    /**
     * Message type: outbox.
     */
    public static final int MESSAGE_TYPE_OUTBOX = 4;

    /**
     * Message type: failed outgoing message.
     */
    public static final int MESSAGE_TYPE_FAILED = 5;

    /**
     * Message type: queued to send later.
     */
    public static final int MESSAGE_TYPE_QUEUED = 6;

    /**
     * MMS address parsing data structures
     */
    // allowable phone number separators
    private static final char[] NUMERIC_CHARS_SUGAR = {
            '-', '.', ',', '(', ')', ' ', '/', '\\', '*', '#', '+'
    };

    private static String[] sNoSubjectStrings;

    public static void markSmsSeen(Context context) {
        Cursor cursor = context.getContentResolver().query(RECEIVED_MESSAGE_CONTENT_PROVIDER,
                new String[]{SmsHelper.COLUMN_ID}, SmsHelper.UNSEEN_SELECTION + " AND " + SmsHelper.UNREAD_SELECTION, null, null);

        if (cursor == null) {
            Log.i(TAG, "No unseen messages");
            return;
        }

        MessageColumns.ColumnsMap map = new MessageColumns.ColumnsMap(cursor);

        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put("seen", true);

            do {
                context.getContentResolver().update(Uri.parse("content://sms/" + cursor.getLong(map.mColumnMsgId)), cv, null, null);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    public static void markMmsSeen(Context context) {
        Cursor cursor = context.getContentResolver().query(MMS_CONTENT_PROVIDER,
                new String[]{SmsHelper.COLUMN_ID}, SmsHelper.UNSEEN_SELECTION + " AND " + SmsHelper.UNREAD_SELECTION, null, null);

        if (cursor == null) {
            Log.i(TAG, "No unseen messages");
            return;
        }

        MessageColumns.ColumnsMap map = new MessageColumns.ColumnsMap(cursor);

        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put("seen", true);

            do {
                context.getContentResolver().update(Uri.parse("content://mms/" + cursor.getLong(map.mColumnMsgId)), cv, null, null);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    public static Settings getSendSettings(Context context) {
        if (sendSettings == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            sendSettings = new Settings();
            sendSettings.setMmsc(prefs.getString(SettingsFragment.MMSC_URL, ""));
            sendSettings.setProxy(prefs.getString(SettingsFragment.MMS_PROXY, ""));
            sendSettings.setPort(prefs.getString(SettingsFragment.MMS_PORT, ""));
            sendSettings.setAgent(prefs.getString("mms_agent", ""));
            sendSettings.setUserProfileUrl(prefs.getString("mms_user_agent_profile_url", ""));
            sendSettings.setUaProfTagName(prefs.getString("mms_user_agent_tag_name", ""));
            sendSettings.setGroup(prefs.getBoolean(SettingsFragment.COMPOSE_GROUP, true));
            setMaxAttachmentSizeSetting(context, prefs.getString(SettingsFragment.MAX_MMS_ATTACHMENT_SIZE, "300kb"));
            sendSettings.setDeliveryReports(prefs.getBoolean(SettingsFragment.DELIVERY_REPORTS, false));
            sendSettings.setSplit(prefs.getBoolean(SettingsFragment.SPLIT_SMS, true));
            sendSettings.setSplitCounter(prefs.getBoolean(SettingsFragment.SPLIT_COUNTER, true));
            sendSettings.setStripUnicode(prefs.getBoolean(SettingsFragment.STRIP_UNICODE, false));
            sendSettings.setSignature(prefs.getString("pref_key_signature", ""));
            sendSettings.setSendLongAsMms(prefs.getBoolean(SettingsFragment.LONG_AS_MMS, false));
            sendSettings.setSendLongAsMmsAfter(Integer.parseInt(prefs.getString(SettingsFragment.LONG_AS_MMS_AFTER, "3")));
            sendSettings.setAccount(null);
            sendSettings.setRnrSe(null);
        }
        return sendSettings;
    }

    /**
     * Sets the max MMS attachment size in the MMS settings field.
     *
     * @param context
     * @param maxAttachmentSize The String value in the ListPreference for max attachment sizes
     */
    public static void setMaxAttachmentSizeSetting(Context context, String maxAttachmentSize) {

        // Initialize sendSettings if it hasn't already been initialized
        sendSettings = getSendSettings(context);

        if (MAX_MMS_ATTACHMENT_SIZE_300KB.equals(maxAttachmentSize)) {
            sendSettings.setMaxAttachmentSize(Settings.MAX_ATTACHMENT_SIZE_300KB);
        } else if (MAX_MMS_ATTACHMENT_SIZE_600KB.equals(maxAttachmentSize)) {
            sendSettings.setMaxAttachmentSize(Settings.MAX_ATTACHMENT_SIZE_600KB);
        } else if (MAX_MMS_ATTACHMENT_SIZE_1MB.equals(maxAttachmentSize)) {
            sendSettings.setMaxAttachmentSize(Settings.MAX_ATTACHMENT_SIZE_1MB);
        } else if (MAX_MMS_ATTACHMENT_SIZE_UNLIMITED.equals(maxAttachmentSize)) {
            sendSettings.setMaxAttachmentSize(Settings.MAX_ATTACHMENT_SIZE_UNLIMITED);
        }
    }

    /**
     * Add incoming SMS to inbox
     *
     * @param context
     * @param address Address of sender
     * @param body    Body of incoming SMS message
     * @param time    Time that incoming SMS message was sent at
     */
    public static Uri addMessageToInbox(Context context, String address, String body, long time) {

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();

        cv.put("address", address);
        cv.put("body", body);
        cv.put("date_sent", time);

        return contentResolver.insert(RECEIVED_MESSAGE_CONTENT_PROVIDER, cv);
    }

    /**
     * Returns true iff the folder (message type) identifies an
     * outgoing message.
     *
     * @hide
     */
    public static boolean isOutgoingFolder(int messageType) {
        return (messageType == MESSAGE_TYPE_FAILED)
                || (messageType == MESSAGE_TYPE_OUTBOX)
                || (messageType == MESSAGE_TYPE_SENT)
                || (messageType == MESSAGE_TYPE_QUEUED);
    }

    public static int getUnseenSMSCount(Context context, long threadId) {
        Cursor cursor = null;
        int count = 0;
        String selection = UNSEEN_SELECTION + " AND " + UNREAD_SELECTION + (threadId == 0 ? "" : " AND " + COLUMN_THREAD_ID + " = " + threadId);

        try {
            cursor = context.getContentResolver().query(RECEIVED_MESSAGE_CONTENT_PROVIDER, new String[]{COLUMN_ID}, selection, null, null);
            cursor.moveToFirst();
            count = cursor.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Returns a string containing the last 10 messages for a given conversation
     * This is used to be displayed on the notification page on a wearable, which
     * only accepts a single String to be displayed
     */
    public static String getHistoryForWearable(Context context, String name, long threadId) {
        final String me = context.getString(R.string.me);
        Cursor cursor = null;
        StringBuilder builder = new StringBuilder();
        MessageColumns.ColumnsMap map = new MessageColumns.ColumnsMap();


        try {
            cursor = context.getContentResolver().query(
                    Uri.withAppendedPath(Message.MMS_SMS_CONTENT_PROVIDER, "" + threadId),
                    MessageColumns.PROJECTION, null, null, "normalized_date DESC LIMIT 10");

            cursor.moveToLast();
            do {
                if (cursor.getString(map.mColumnMsgType).equals("sms")) {
                    int boxId = cursor.getInt(map.mColumnSmsType);
                    boolean in = boxId == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX ||
                            boxId == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_ALL;

                    builder.append(in ? name : me)
                            .append("\n")
                            .append(cursor.getString(map.mColumnSmsBody))
                            .append("\n\n");
                }
            } while (cursor.moveToPrevious());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return builder.toString();
    }

    /**
     * List of messages grouped by thread id, used for showing notifications
     */
    public static HashMap<Long, ArrayList<MessageItem>> getUnreadUnseenConversations(Context context) {
        HashMap<Long, ArrayList<MessageItem>> result = new HashMap<>();

        String selection = SmsHelper.UNSEEN_SELECTION + " AND " + SmsHelper.UNREAD_SELECTION;

        // Create a cursor for the conversation list
        Cursor conversationCursor = context.getContentResolver().query(
                SmsHelper.CONVERSATIONS_CONTENT_PROVIDER, Conversation.ALL_THREADS_PROJECTION,
                SmsHelper.UNREAD_SELECTION, null, SmsHelper.sortDateAsc);

        if (conversationCursor != null && conversationCursor.moveToFirst()) {
            do {
                ArrayList<MessageItem> messages = new ArrayList<>();
                long threadId = conversationCursor.getLong(Conversation.ID);
                Uri threadUri = Uri.withAppendedPath(Message.MMS_SMS_CONTENT_PROVIDER, Long.toString(threadId));
                Cursor messageCursor = context.getContentResolver().query(threadUri, MessageColumns.PROJECTION, selection, null, SmsHelper.sortDateAsc);

                if (messageCursor != null && messageCursor.moveToFirst()) {
                    do {
                        MessageColumns.ColumnsMap columnsMap = new MessageColumns.ColumnsMap(messageCursor);
                        MessageItem message = null;
                        try {
                            message = new MessageItem(context, messageCursor.getString(columnsMap.mColumnMsgType), messageCursor, columnsMap, null, true);
                        } catch (MmsException e) {
                            e.printStackTrace();
                        }
                        messages.add(message);
                    } while (messageCursor.moveToNext());
                    messageCursor.close();
                    result.put(threadId, messages);
                }

            } while (conversationCursor.moveToNext());
            conversationCursor.close();
        }

        return result;
    }

    /**
     * @return A list of unread messages to be deleted by QKReply
     */
    public static ArrayList<Message> getUnreadMessagesLegacy(Context context, Uri threadUri) {
        ArrayList<Message> result = new ArrayList<>();

        if (threadUri != null) {
            Cursor messageCursor = context.getContentResolver().query(threadUri, MessageColumns.PROJECTION, UNREAD_SELECTION, null, SmsHelper.sortDateAsc);
            MessageColumns.ColumnsMap columnsMap = new MessageColumns.ColumnsMap(messageCursor);

            if (messageCursor.moveToFirst()) {
                do {
                    try {
                        Message message = new Message(context, messageCursor.getLong(columnsMap.mColumnMsgId));
                        result.add(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (messageCursor.moveToNext());
            }

            messageCursor.close();
        }

        return result;
    }

    public static String getUnreadMessageText(Context context, Uri threadUri) {
        StringBuilder builder = new StringBuilder();

        ArrayList<Message> messages = SmsHelper.getUnreadMessagesLegacy(context, threadUri);
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            builder.append(message.getBody());
            if (i < messages.size() - 1) {
                builder.append("\n\n");
            }
        }

        return builder.toString();
    }

    public static long getThreadId(Context context, String address) {
        Cursor cursor = null;
        long threadId = 0;

        try {
            cursor = context.getContentResolver().query(SENT_MESSAGE_CONTENT_PROVIDER, new String[]{COLUMN_THREAD_ID}, COLUMN_ADDRESS + "=" + address, null, sortDateDesc);
            cursor.moveToFirst();
            threadId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_THREAD_ID));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return threadId;
    }

    public static int getUnreadMessageCount(Context context) {
        int result = 0;

        // Create a cursor for the conversation list
        Cursor conversationCursor = context.getContentResolver().query(
                SmsHelper.CONVERSATIONS_CONTENT_PROVIDER, Conversation.ALL_THREADS_PROJECTION,
                SmsHelper.UNREAD_SELECTION, null, SmsHelper.sortDateAsc);

        if (conversationCursor.moveToFirst()) {
            do {
                Uri threadUri = Uri.withAppendedPath(Message.MMS_SMS_CONTENT_PROVIDER, conversationCursor.getString(Conversation.ID));
                Cursor messageCursor = context.getContentResolver().query(threadUri, MessageColumns.PROJECTION, SmsHelper.UNREAD_SELECTION, null, SmsHelper.sortDateDesc);
                if (messageCursor != null) {
                    result += messageCursor.getCount();
                    messageCursor.close();
                }
            } while (conversationCursor.moveToNext());
        }

        conversationCursor.close();

        return result;
    }

    public static List<Message> getFailedMessages(Context context) {
        Cursor cursor = null;
        List<Message> messages = new ArrayList<>();

        try {
            cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{COLUMN_ID}, FAILED_SELECTION, null, sortDateDesc);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                messages.add(new Message(context, cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return messages;
    }

    public static List<Message> deleteFailedMessages(Context context, long threadId) {
        Log.d(TAG, "Deleting failed messages");
        Cursor cursor = null;
        List<Message> messages = new ArrayList<>();

        try {
            cursor = context.getContentResolver().query(SMS_CONTENT_PROVIDER, new String[]{COLUMN_ID}, FAILED_SELECTION, null, sortDateDesc);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                messages.add(new Message(context, cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (Message m : messages) {
            if (m.getThreadId() == threadId) {
                Log.d(TAG, "Deleting failed message to " + m.getName() + "\n Body: " + m.getBody());
                m.delete();
            }
        }
        return messages;
    }

    /**
     * Add an SMS to the given URI.
     *
     * @param resolver       the content resolver to use
     * @param uri            the URI to add the message to
     * @param address        the address of the sender
     * @param body           the body of the message
     * @param subject        the pseudo-subject of the message
     * @param date           the timestamp for the message
     * @param read           true if the message has been read, false if not
     * @param deliveryReport true if a delivery report was requested, false if not
     * @return the URI for the new message
     * @hide
     */
    public static Uri addMessageToUri(ContentResolver resolver,
                                      Uri uri, String address, String body, String subject,
                                      Long date, boolean read, boolean deliveryReport) {
        return addMessageToUri(resolver, uri, address, body, subject,
                date, read, deliveryReport, -1L);
    }

    /**
     * Add an SMS to the given URI with the specified thread ID.
     *
     * @param resolver       the content resolver to use
     * @param uri            the URI to add the message to
     * @param address        the address of the sender
     * @param body           the body of the message
     * @param subject        the pseudo-subject of the message
     * @param date           the timestamp for the message
     * @param read           true if the message has been read, false if not
     * @param deliveryReport true if a delivery report was requested, false if not
     * @param threadId       the thread_id of the message
     * @return the URI for the new message
     * @hide
     */
    public static Uri addMessageToUri(ContentResolver resolver,
                                      Uri uri, String address, String body, String subject,
                                      Long date, boolean read, boolean deliveryReport, long threadId) {
        ContentValues values = new ContentValues(7);

        values.put(Sms.ADDRESS, address);
        if (date != null) {
            values.put(Sms.DATE, date);
        }
        values.put(Sms.READ, read ? Integer.valueOf(1) : Integer.valueOf(0));
        values.put(Sms.SUBJECT, subject);
        values.put(Sms.BODY, body);
        if (deliveryReport) {
            values.put(Sms.STATUS, Sms.STATUS_PENDING);
        }
        if (threadId != -1L) {
            values.put(Sms.THREAD_ID, threadId);
        }
        return resolver.insert(uri, values);
    }

    public static String extractEncStrFromCursor(Cursor cursor,
                                                 int columnRawBytes, int columnCharset) {
        String rawBytes = cursor.getString(columnRawBytes);
        int charset = cursor.getInt(columnCharset);

        if (TextUtils.isEmpty(rawBytes)) {
            return "";
        } else if (charset == CharacterSets.ANY_CHARSET) {
            return rawBytes;
        } else {
            return new EncodedStringValue(charset, PduPersister.getBytes(rawBytes)).getString();
        }
    }

    /**
     * cleanseMmsSubject will take a subject that's says, "<Subject: no subject>", and return
     * a null string. Otherwise it will return the original subject string.
     *
     * @param context a regular context so the function can grab string resources
     * @param subject the raw subject
     * @return
     */
    public static String cleanseMmsSubject(Context context, String subject) {
        if (TextUtils.isEmpty(subject)) {
            return subject;
        }
        if (sNoSubjectStrings == null) {
            sNoSubjectStrings = context.getResources().getStringArray(R.array.empty_subject_strings);
        }

        final int len = sNoSubjectStrings.length;
        for (int i = 0; i < len; i++) {
            if (subject.equalsIgnoreCase(sNoSubjectStrings[i])) {
                return null;
            }
        }
        return subject;
    }

    /**
     * Is the specified address an email address?
     *
     * @param address the input address to test
     * @return true if address is an email address; false otherwise.
     * @hide
     */
    public static boolean isEmailAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }

        String s = extractAddrSpec(address);
        Matcher match = Patterns.EMAIL_ADDRESS.matcher(s);
        return match.matches();
    }

    /**
     * Regex pattern for names and email addresses.
     * <ul>
     * <li><em>mailbox</em> = {@code name-addr}</li>
     * <li><em>name-addr</em> = {@code [display-name] angle-addr}</li>
     * <li><em>angle-addr</em> = {@code [CFWS] "<" addr-spec ">" [CFWS]}</li>
     * </ul>
     *
     * @hide
     */
    public static final Pattern NAME_ADDR_EMAIL_PATTERN =
            Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");

    /**
     * Helper method to extract email address from address string.
     *
     * @hide
     */
    public static String extractAddrSpec(String address) {
        Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);

        if (match.matches()) {
            return match.group(2);
        }
        return address;
    }

    private static HashMap numericSugarMap = new HashMap(NUMERIC_CHARS_SUGAR.length);

    /**
     * Given a phone number, return the string without syntactic sugar, meaning parens,
     * spaces, slashes, dots, dashes, etc. If the input string contains non-numeric
     * non-punctuation characters, return null.
     */
    private static String parsePhoneNumberForMms(String address) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();

        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);

            // accept the first '+' in the address
            if (c == '+' && builder.length() == 0) {
                builder.append(c);
                continue;
            }

            if (Character.isDigit(c)) {
                builder.append(c);
                continue;
            }

            if (numericSugarMap.get(c) == null) {
                return null;
            }
        }
        return builder.toString();
    }

    /**
     * parse the input address to be a valid MMS address.
     * - if the address is an email address, leave it as is.
     * - if the address can be parsed into a valid MMS phone number, return the parsed number.
     * - if the address is a compliant alias address, leave it as is.
     */
    public static String parseMmsAddress(String address) {
        // if it's a valid Email address, use that.
        if (isEmailAddress(address)) {
            return address;
        }

        // if we are able to parse the address to a MMS compliant phone number, take that.
        String retVal = parsePhoneNumberForMms(address);
        if (retVal != null && retVal.length() != 0) {
            return retVal;
        }

        // if it's an alias compliant address, use that.
        if (isAlias(address)) {
            return address;
        }

        // it's not a valid MMS address, return null
        return null;
    }

    // An alias (or commonly called "nickname") is:
    // Nickname must begin with a letter.
    // Only letters a-z, numbers 0-9, or . are allowed in Nickname field.
    public static boolean isAlias(String string) {
        if (!MmsConfig.isAliasEnabled()) {
            return false;
        }

        int len = string == null ? 0 : string.length();

        if (len < MmsConfig.getAliasMinChars() || len > MmsConfig.getAliasMaxChars()) {
            return false;
        }

        if (!Character.isLetter(string.charAt(0))) {    // Nickname begins with a letter
            return false;
        }
        for (int i = 1; i < len; i++) {
            char c = string.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '.')) {
                return false;
            }
        }

        return true;
    }

    public static int getAttachmentType(SlideshowModel model, MultimediaMessagePdu mmp) {
        if (model == null || mmp == null) {
            return MessageItem.ATTACHMENT_TYPE_NOT_LOADED;
        }

        int numberOfSlides = model.size();
        if (numberOfSlides > 1) {
            return SLIDESHOW;
        } else if (numberOfSlides == 1) {
            // Only one slide in the slide-show.
            SlideModel slide = model.get(0);
            if (slide.hasVideo()) {
                return VIDEO;
            }

            if (slide.hasAudio() && slide.hasImage()) {
                return SLIDESHOW;
            }

            if (slide.hasAudio()) {
                return AUDIO;
            }

            if (slide.hasImage()) {
                return IMAGE;
            }

            if (slide.hasText()) {
                return TEXT;
            }

            // Handle the multimedia message only has subject
            String subject = mmp.getSubject() != null ? mmp.getSubject().getString() : null;
            if (!TextUtils.isEmpty(subject)) {
                return TEXT;
            }
        }

        return MessageItem.ATTACHMENT_TYPE_NOT_LOADED;
    }

    /**
     * Returns the position of the message in the cursor.
     *
     * @param cursor
     * @param messageType
     * @param messageId
     * @param map
     * @return
     */
    public static int getPositionForMessageId(Cursor cursor, String messageType, long messageId, MessageColumns.ColumnsMap map) {

        // Modified binary search on the cursor to find the position of the message in the cursor.
        // It's modified because, although the SMS and MMS are generally ordered in terms of their
        // ID, they have different IDs. So, we might have a list of IDs like:
        //
        // [ 4444, 4447, 4449, 4448, 312, 315, 4451 ]
        //
        // where the 44xx IDs are for SMS messages, and the 31x IDs are for MMS messages. The
        // solution is to do a linear scan if we reach a point in the list where the ID doesn't
        // match what we're looking for.

        // Lower and upper bounds for doing the search
        int min = 0;
        int max = cursor.getCount() - 1;

        while (min <= max) {
            int mid = min / 2 + max / 2 + (min & max & 1);

            cursor.moveToPosition(mid);
            long candidateId = cursor.getLong(map.mColumnMsgId);
            String candidateType = cursor.getString(map.mColumnMsgType);

            if (messageType.equals(candidateType)) {
                if (messageId < candidateId) {
                    max = mid - 1;
                } else if (messageId > candidateId) {
                    min = mid + 1;
                } else {
                    return mid;
                }

            } else {
                // This message is the wrong type, so we have to do a linear search until we find a
                // message that is the right type so we can orient ourselves.

                // First, look forward. Stop when we move past max, or reach the end of the cursor.
                boolean success = false;
                while (cursor.getPosition() <= max && cursor.moveToNext()) {
                    candidateType = cursor.getString(map.mColumnMsgType);
                    if (candidateType.equals(messageType)) {
                        success = true;
                        break;
                    }
                }

                if (!success) {
                    // We didn't find any messages of the right type by looking forward, so try
                    // looking backwards.
                    cursor.moveToPosition(mid);
                    while (cursor.getPosition() >= min && cursor.moveToPrevious()) {
                        candidateType = cursor.getString(map.mColumnMsgType);
                        if (candidateType.equals(messageType)) {
                            success = true;
                            break;
                        }
                    }
                }

                if (!success) {
                    // There is no message with that ID of the correct type!
                    return -1;
                }

                // In this case, we've found a message of the correct type! Now to do the binary
                // search stuff.
                candidateId = cursor.getLong(map.mColumnMsgId);
                int pos = cursor.getPosition();
                if (messageId < candidateId) {
                    // The new upper bound is the minimum of where we started and where we ended 
                    // up, subtract one.
                    max = (mid < pos ? mid : pos) - 1;
                } else if (messageId > candidateId) {
                    // Same as above but in reverse.
                    min = (mid > pos ? mid : pos) + 1;
                } else {
                    return pos;
                }
            }
        }

        // This is the case where we've minimized our bounds until they're the same, and we haven't
        // found anything yet---this means that the item doesn't exist, so return -1.
        return -1;
    }
}
